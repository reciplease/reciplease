import types from '../constants/';

let ingredientId = 0;

const nextId = () => {
  ingredientId += 1;
  return ingredientId;
};

const actions = {
  submitIngredient(ingredient) {
    return {
      type: types.SUBMIT_INGREDIENT,
      ingredient,
    };
  },
};

export default actions;
