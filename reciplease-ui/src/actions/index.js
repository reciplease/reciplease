import types from '../constants/';

const actions = {
  submitIngredient(ingredient) {
    return {
      type: types.SUBMIT_INGREDIENT,
      ingredient,
    };
  },
};

export default actions;
