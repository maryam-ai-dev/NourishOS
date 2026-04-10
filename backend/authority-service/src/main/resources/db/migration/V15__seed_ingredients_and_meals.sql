-- Seed additional ingredients
INSERT INTO ingredients (id, name, category, default_unit, perishability_class) VALUES
    ('a0000001-0000-0000-0000-000000000001', 'Chicken Breast', 'countables', 'g', 'HIGHLY_PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000002', 'Rice', 'dry_goods', 'g', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000003', 'Pasta', 'dry_goods', 'g', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000004', 'Tomato', 'countables', 'unit', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000005', 'Onion', 'countables', 'unit', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000006', 'Garlic', 'countables', 'unit', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000007', 'Bell Pepper', 'countables', 'unit', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000008', 'Salmon Fillet', 'countables', 'g', 'HIGHLY_PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000009', 'Broccoli', 'countables', 'unit', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000010', 'Butter', 'dry_goods', 'g', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000011', 'Milk', 'liquids', 'ml', 'HIGHLY_PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000012', 'Cheese', 'dry_goods', 'g', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000013', 'Lemon', 'countables', 'unit', 'PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000014', 'Spinach', 'dry_goods', 'g', 'HIGHLY_PERISHABLE'),
    ('a0000001-0000-0000-0000-000000000015', 'Potato', 'countables', 'unit', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000016', 'Soy Sauce', 'liquids', 'ml', 'SHELF_STABLE'),
    ('a0000001-0000-0000-0000-000000000017', 'Honey', 'liquids', 'ml', 'SHELF_STABLE')
ON CONFLICT (name) DO NOTHING;

-- Seed 20 MealOptions with valid ingredientRefs
INSERT INTO meal_options (name, meal_type, estimated_protein_grams, estimated_calories, prep_time_minutes, sustainability_score, ingredient_refs) VALUES
('Chicken Stir Fry', 'DINNER', 42, 480, 25, 0.65,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000001","baseQuantity":200,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000007","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000016","baseQuantity":15,"unit":"ml","optional":false,"substitutable":false}]'),

('Salmon with Rice', 'DINNER', 38, 520, 30, 0.55,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000008","baseQuantity":180,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000002","baseQuantity":150,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000013","baseQuantity":1,"unit":"unit","optional":true,"substitutable":false}]'),

('Pasta Carbonara', 'DINNER', 28, 650, 20, 0.70,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000003","baseQuantity":200,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000012","baseQuantity":50,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":20,"unit":"g","optional":false,"substitutable":false}]'),

('Vegetable Omelette', 'BREAKFAST', 22, 320, 15, 0.85,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000007","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000005","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":10,"unit":"g","optional":false,"substitutable":false}]'),

('Rice Bowl with Chicken', 'LUNCH', 35, 450, 20, 0.72,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000001","baseQuantity":150,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000002","baseQuantity":200,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000016","baseQuantity":10,"unit":"ml","optional":true,"substitutable":false}]'),

('Spinach Smoothie', 'BREAKFAST', 8, 180, 5, 0.95,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000014","baseQuantity":60,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000011","baseQuantity":200,"unit":"ml","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000017","baseQuantity":15,"unit":"ml","optional":true,"substitutable":false}]'),

('Baked Potato', 'LUNCH', 6, 350, 45, 0.90,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000015","baseQuantity":2,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":15,"unit":"g","optional":true,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000012","baseQuantity":30,"unit":"g","optional":true,"substitutable":true}]'),

('Garlic Butter Salmon', 'DINNER', 40, 420, 25, 0.60,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000008","baseQuantity":200,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000006","baseQuantity":3,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":20,"unit":"g","optional":false,"substitutable":false}]'),

('Broccoli Cheese Soup', 'LUNCH', 14, 280, 30, 0.80,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000009","baseQuantity":2,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000012","baseQuantity":60,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000011","baseQuantity":150,"unit":"ml","optional":false,"substitutable":true}]'),

('Tomato Pasta', 'DINNER', 15, 420, 20, 0.82,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000003","baseQuantity":200,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000004","baseQuantity":3,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000006","baseQuantity":2,"unit":"unit","optional":false,"substitutable":false}]'),

('Honey Garlic Chicken', 'DINNER', 38, 400, 30, 0.68,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000001","baseQuantity":200,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000017","baseQuantity":30,"unit":"ml","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000006","baseQuantity":3,"unit":"unit","optional":false,"substitutable":false}]'),

('Cheese Omelette', 'BREAKFAST', 24, 350, 10, 0.88,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000012","baseQuantity":40,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":10,"unit":"g","optional":false,"substitutable":false}]'),

('Lemon Herb Salmon', 'DINNER', 36, 380, 20, 0.58,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000008","baseQuantity":180,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000013","baseQuantity":1,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000014","baseQuantity":30,"unit":"g","optional":true,"substitutable":true}]'),

('Potato Soup', 'LUNCH', 8, 300, 35, 0.88,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000015","baseQuantity":3,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000005","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000011","baseQuantity":200,"unit":"ml","optional":false,"substitutable":true}]'),

('Chicken Salad', 'LUNCH', 30, 280, 15, 0.78,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000001","baseQuantity":150,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000014","baseQuantity":50,"unit":"g","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000013","baseQuantity":1,"unit":"unit","optional":true,"substitutable":false}]'),

('Buttered Rice', 'DINNER', 5, 380, 15, 0.92,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000002","baseQuantity":200,"unit":"g","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":15,"unit":"g","optional":false,"substitutable":false}]'),

('Stir Fry Vegetables', 'DINNER', 6, 200, 15, 0.95,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000007","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000009","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000005","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000016","baseQuantity":10,"unit":"ml","optional":false,"substitutable":false}]'),

('Milk Porridge', 'BREAKFAST', 10, 250, 10, 0.90,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000011","baseQuantity":250,"unit":"ml","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000017","baseQuantity":10,"unit":"ml","optional":true,"substitutable":false}]'),

('Garlic Bread', 'SNACK', 4, 180, 10, 0.85,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000006","baseQuantity":2,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000010","baseQuantity":20,"unit":"g","optional":false,"substitutable":false}]'),

('Tomato Soup', 'LUNCH', 4, 150, 25, 0.92,
 '[{"ingredientId":"a0000001-0000-0000-0000-000000000004","baseQuantity":4,"unit":"unit","optional":false,"substitutable":false},{"ingredientId":"a0000001-0000-0000-0000-000000000005","baseQuantity":1,"unit":"unit","optional":false,"substitutable":true},{"ingredientId":"a0000001-0000-0000-0000-000000000006","baseQuantity":2,"unit":"unit","optional":false,"substitutable":false}]')

ON CONFLICT (name) DO NOTHING;
