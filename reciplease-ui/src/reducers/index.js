import types from '../constants/'

export const initialState = {
  ingredients: [],
};

export const reducer = (state = initialState, action) => {
  switch (action.type) {
    case types.SUBMIT_INGREDIENT:
      return {
        ...state,
        ingredients: [
          ...state.ingredients,
          {
            id: action.id,
            text: action.text,
          }
        ]
      };
    default:
      return state;
  }
};

export default reducer;
