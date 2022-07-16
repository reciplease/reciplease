# DB Schema

## Enums

### Measure
- ITEMS
- KILOGRAMS
- GRAMS
- LITRES
- MILLILITRES

### Mealtime
- Breakfast
- Lunch
- Dinner
- Supper
- Snacks

## Tables

### BaseEntity
- uuid

### Recipe

- recipe_uuid
- recipe_name

### Inventory (ShoppingList)


#### ShoppingInventory


### Ingredient

- ingredient_uuid
- ingredient_name
- baseMeasure



#### RecipeIngredient (RecipeItem)

#### InventoryIngredient (InventoryItem)

## Joins

### RecipeSchedule (PlannedRecipe)

- recipe_uuid
- date
- mealtime


