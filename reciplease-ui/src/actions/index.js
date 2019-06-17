import types from '../constants/';

let ingredientId = 0;

const nextId = () => {
  ingredientId += 1;
  return ingredientId;
};

const actions = {
  submitIngredient(text) {
    return {
      type: types.SUBMIT_INGREDIENT,
      id: nextId(),
      text,
    };
  },
};

export default actions;
