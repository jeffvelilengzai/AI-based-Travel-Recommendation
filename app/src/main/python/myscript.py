import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import MultiLabelBinarizer
from pulp import LpMaximize, LpProblem, LpVariable, lpSum
from datetime import datetime, timedelta
from io import StringIO
import json

#from pulp import LpProblem, LpVariable, lpSum, LpStatus, GLPK


# Function to read CSV content from assets
def load_data(context, file_name):
    from java.io import InputStreamReader, BufferedReader
    from java.lang import StringBuilder

    asset_manager = context.getAssets()
    input_stream = asset_manager.open(file_name)
    reader = BufferedReader(InputStreamReader(input_stream))

    # Read the entire CSV content as a string
    sb = StringBuilder()
    line = reader.readLine()
    while line is not None:
        sb.append(line).append('\n')
        line = reader.readLine()

    csv_content = sb.toString()

    # Use pandas to read the CSV content
    df = pd.read_csv(StringIO(csv_content), low_memory=False)
    return df


# Function to extract cuisines from the restaurant DataFrame
def extract_cuisines(row):
    cuisines = []
    for i in range(9):  # Assuming you have cuisines/0 to cuisines/8
        cuisine_column = f'cuisines/{i}'
        if cuisine_column in row and pd.notna(row[cuisine_column]):
            cuisines.append(row[cuisine_column].strip().lower())  # Ensure cuisines are lowercased
    return cuisines

# Load and clean restaurant data from CSV content
def load_restaurant_data(csv_contents, context):
    dfs = []
    for file_name in csv_contents:
        df = load_data(context, file_name)
        df['cuisine_types'] = df.apply(extract_cuisines, axis=1)
        df['price'] = df.apply(extract_restaurant_price, axis=1)
        dfs.append(df[['name', 'cuisine_types', 'rating', 'price', 'webUrl']])
    return pd.concat(dfs, ignore_index=True)

# Function to convert price strings to float
def convert_to_float(price_str):
    if isinstance(price_str, (int, float)):
        return float(price_str)
    if isinstance(price_str, str):
        cleaned_price_str = price_str.replace('$', '').replace(',', '').replace('MYR', '').replace('\xa0', '').strip()
        return float(cleaned_price_str)
    return np.nan

# Price extraction for hotels
def extract_hotel_price(row):
    price = row['offers/0/pricePerNight']
    return convert_to_float(price) if pd.notna(price) else np.nan

# Price extraction for restaurants
def extract_restaurant_price(row):
    if 'priceRange' in row and pd.notna(row['priceRange']):
        price_str = row['priceRange'].split('-')[-1].strip()
        return convert_to_float(price_str)
    return np.nan

# Price extraction for attractions
def extract_attraction_price(row):
    price = row['offerGroup/offerList/0/price']
    return convert_to_float(price) if pd.notna(price) else np.nan

# Function to extract price based on type
def extract_price(row):
    if row['type'] == 'HOTEL':
        return extract_hotel_price(row)
    elif row['type'] == 'RESTAURANT':
        return extract_restaurant_price(row)
    elif row['type'] == 'ATTRACTION':
        return extract_attraction_price(row)
    return np.nan

# Load and clean data
def load_and_clean_data(context, file_name, is_hotel=False):
    df = load_data(context, file_name)
    if is_hotel:
        df = df.rename(columns={'hotel_name': 'name', 'city': 'addressObj/city', 'country': 'addressObj/country', 'url': 'website'})
        df['type'] = 'HOTEL'
    relevant_columns = ['name', 'addressObj/city', 'addressObj/country', 'type', 'webUrl', 'website', 'rating']
    if 'offerGroup/offerList/0/price' in df.columns:
        relevant_columns.append('offerGroup/offerList/0/price')
    if 'offers/0/pricePerNight' in df.columns:
        relevant_columns.append('offers/0/pricePerNight')
    if 'priceRange' in df.columns:
        relevant_columns.append('priceRange')
    if 'subcategories/0' in df.columns:
        relevant_columns.append('subcategories/0')
    for i in range(5):
        amenity_column = f'amenities/{i}'
        if amenity_column in df.columns:
            relevant_columns.append(amenity_column)
    df_filtered = df[relevant_columns].dropna(subset=['name', 'type'])
    df_filtered['price'] = df_filtered.apply(extract_price, axis=1)
    df_filtered = df_filtered.dropna(subset=['price'])
    df_filtered['price'] = df_filtered['price'].astype(float)
    return df_filtered


# Function to extract subcategories from the attraction DataFrame
def extract_subcategories(row):
    subcategories = []
    for i in range(1):  # Assuming you have subcategories/0 to subcategories/0
        subcategory_column = f'subcategories/{i}'
        if subcategory_column in row and pd.notna(row[subcategory_column]):
            subcategories.append(row[subcategory_column].strip().lower())  # Ensure subcategories are lowercased
    return subcategories

# Function to calculate cosine similarity between user preferences and attraction subcategories
def find_similar_attractions(df, user_preferences):
    # Convert Java array to Python list
    user_preferences = list(user_preferences)

    mlb = MultiLabelBinarizer()

    # Fit and transform attraction subcategories into a binary matrix
    subcategory_matrix = mlb.fit_transform(df['subcategory_types'])

    # Create a binary vector for user preferences
    user_vector = mlb.transform([user_preferences])

    # Calculate cosine similarity between the user vector and attraction vectors
    similarity_scores = cosine_similarity(user_vector, subcategory_matrix).flatten()

    # Add similarity scores to the DataFrame
    df['similarity_score'] = similarity_scores

    # Sort attractions by similarity score in descending order
    return df[df['similarity_score'] > 0].sort_values(by='similarity_score', ascending=False)

# Linear programming for budget optimization
def heuristic_optimization(df, budget):
    df = df.dropna(subset=['price', 'rating'])

    # Sort items by rating (or other criteria) in descending order
    df = df.sort_values(by='rating', ascending=False)

    selected_items = []
    total_cost = 0

    for _, row in df.iterrows():
        if total_cost + row['price'] <= budget:
            selected_items.append(row)
            total_cost += row['price']

    selected_df = pd.DataFrame(selected_items)
    return selected_df

def find_similar_restaurants(df, user_preferences):

    mlb = MultiLabelBinarizer()

    # Convert Java array to Python list
    user_preferences = list(user_preferences)

    # Fit and transform restaurant cuisines into a binary matrix
    cuisine_matrix = mlb.fit_transform(df['cuisine_types'])

    # Create a binary vector for user preferences
    user_vector = mlb.transform([user_preferences])

    # Calculate cosine similarity between the user vector and restaurant vectors
    similarity_scores = cosine_similarity(user_vector, cuisine_matrix).flatten()

    # Add similarity scores to the DataFrame
    df.loc[:, 'similarity_score'] = similarity_scores

    # Sort restaurants by similarity score in descending order
    return df[df['similarity_score'] > 0].sort_values(by='similarity_score', ascending=False)



def generate_recommendations(context, start_date, end_date, destination, budget, cuisines, subcategories):
    start_date = datetime.strptime(start_date, "%Y-%m-%d")
    end_date = datetime.strptime(end_date, "%Y-%m-%d")

    # Load data for each destination
    ipoh_df = load_and_clean_data(context, 'ipoh.csv')
    penang_df = load_and_clean_data(context, 'penang.csv')
    melaka_df = load_and_clean_data(context, 'melaka.csv')
    ipohhotel_df = load_and_clean_data(context, 'ipohhotel.csv', is_hotel=True)

    if destination.lower() == 'ipoh':
        df = pd.concat([ipoh_df, ipohhotel_df], ignore_index=True)
        restaurant_file = 'ipoh.csv'  # Load only the Ipoh restaurant file
    elif destination.lower() == 'penang':
        df = penang_df
        restaurant_file = 'penang.csv'  # Load only the Penang restaurant file
    elif destination.lower() == 'melaka':
        df = melaka_df
        restaurant_file = 'melaka.csv'  # Load only the Melaka restaurant file
    else:
        return json.dumps({"error": "Destination not found."})

    num_nights = (end_date - start_date).days
    selected_hotel = None
    if num_nights > 0:
        hotels = df[df['type'] == 'HOTEL']
        if not hotels.empty:
            affordable_hotels = hotels[hotels['price'] * num_nights <= budget]
            if not affordable_hotels.empty:
                affordable_hotels = affordable_hotels.sort_values(by='rating', ascending=False)
                selected_hotel = affordable_hotels.iloc[0]
                budget -= selected_hotel['price'] * num_nights

    # Load restaurant data from input destination files
    restaurant_files = [restaurant_file]
    restaurant_df = load_restaurant_data(restaurant_files, context)

    # Find similar restaurants based on user preferences
    recommended_restaurants = find_similar_restaurants(restaurant_df, cuisines)

    # Filter out attractions
    attractions = df[df['type'] == 'ATTRACTION'].copy()

    # Extract subcategories into a new column
    attractions.loc[:, 'subcategory_types'] = attractions.apply(extract_subcategories, axis=1)

    # Find similar attractions based on user preferences
    recommended_attractions = find_similar_attractions(attractions, subcategories)

    # Apply budget optimization
    optimized_restaurants = heuristic_optimization(recommended_restaurants, budget)
    optimized_attractions = heuristic_optimization(recommended_attractions, budget)

    recommendations = {}
    all_restaurants = optimized_restaurants.replace({np.nan: None}).to_dict(orient='records')
    all_attractions = optimized_attractions.replace({np.nan: None}).to_dict(orient='records')
    all_hotels = affordable_hotels.iloc[1:].replace({np.nan: None}).to_dict(orient='records') if affordable_hotels is not None and len(affordable_hotels) > 1 else []

    # Processing all hotels with the same logic used for the selected hotel
    for hotel in all_hotels:
        hotel['price_per_night'] = hotel.get('price', 'N/A')
        hotel['total_price'] = hotel['price'] * num_nights if 'price' in hotel else 'N/A'
        hotel['checkin_checkout'] = f"{start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}"
        hotel['amenities'] = [hotel.get(f'amenities/{i}', 'N/A') for i in range(5)]  # Include amenities from 0 to 4


    # Update the 'cuisines' field for all restaurants in all_restaurants
    for restaurant in all_restaurants:
        restaurant['cuisines'] = restaurant['cuisine_types']

    for attraction in all_attractions:
        attraction['subcategory'] = attraction.get('subcategories/0', 'N/A')  # Get subcategory

    num_days = (end_date - start_date).days + 1

    for day in range(num_days):
        current_date = start_date + timedelta(days=day)
        formatted_date = current_date.strftime('%Y-%m-%d')

        day_recommendations = {}

        if day == 0 and selected_hotel is not None:
            day_recommendations['Hotel'] = {
                'name': selected_hotel['name'],
                'type': selected_hotel['type'],
                'price_per_night': selected_hotel['price'],
                'total_price': selected_hotel['price'] * num_nights,
                'rating': selected_hotel.get('rating', 'N/A'),
                'website': selected_hotel['website'],
                'checkin_checkout': f"{start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}",
                'amenities': [selected_hotel.get(f'amenities/{i}', 'N/A') for i in range(5)]  # Include amenities from 0 to 4
            }


        if len(all_restaurants) > 0:
            selected_restaurants = all_restaurants[:3]
            all_restaurants = all_restaurants[3:]  # Remove the top 3
            for restaurant in selected_restaurants:
                restaurant['cuisines'] = restaurant['cuisine_types']
            day_recommendations['Restaurants'] = selected_restaurants
        else:
            day_recommendations['Restaurants'] = "No suitable restaurants found within budget."

        if len(all_attractions) > 0:
            selected_attractions = all_attractions[:2]
            all_attractions = all_attractions[2:]  # Remove the top 2
            for attraction in selected_attractions:
                attraction['subcategory'] = attraction.get('subcategories/0', 'N/A')  # Get subcategory
            day_recommendations['Attractions'] = selected_attractions
        else:
            day_recommendations['Attractions'] = "No suitable attractions found within budget."

        recommendations[formatted_date] = day_recommendations

    return json.dumps({
        "recommendations": recommendations,
        "all_restaurants": all_restaurants,  # Store the remaining restaurants
        "all_attractions": all_attractions,
        "all_hotels": all_hotels  # Store the remaining hotels
    })