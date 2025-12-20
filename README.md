# PantryChef-MWC

## Steps to install and run the app
- The google-services.json is required for Firebase. Place the file in /app .
- final_recipes.csv and ingredients.csv is required for the initial setup. Place both the files in app/src/main/assests.
- Place your Gemini API key in the local.properties file in the root of the project.Also change the sdk directory.
- Add the link to jitpack i.e maven { url = uri("https://jitpack.io") } to the dependencyResolutionManagement in settings.gradle.kts

Link to both the files are forwarded to the TAs on the email.

### Login
- Used Firebase to setup google login.
- After login the first setup of database takes place which may take 5-10 mins. For more information open logcat to see the population happening.

### Step Counter
- Count user steps taken.
- Currently the goal is hard coded at 100.

### Profile

- Login is 
- User information.
- Required nutrient intake information.


### Visualizations
- Bar chart for user pantry items
- Line chart for daily steps taken

### Pantry
- Shows different categories of items.
- Inside the category items can be edited and if quantity is made 0 then removed from the pantry.
- User can add items manually or by adding image from gallery or clicking a photo using the camera.
- When user adds item manually then they can only add items from the master list created during database setup so garbage values are not allowed.

### Recipes
- Queries the database to recommend recipes to the user based on the pantry.
- Can select number of servings for recipes.




#### Shortcomings
- Could not link the step counter and recipe recommender to recommend recipes based on calories burned.
- Master list of items contains all the necessary names but when adding manually some items are not recognized like egg,salt,pepper etc. which we have no idea why.
- Could not complete the implementation to show exact quantities of ingredients to use but it is available in the data present in the assests just need to create a view for quants and add it to recipe details using the repository of our database.
- Tried to make database queries faster but could not find a more efficient way to do so.
