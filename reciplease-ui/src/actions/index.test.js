/* global expect, it, describe */

import types from '../constants/';
import actions from '.';

describe('Actions', () => {
  const ingredientName = 'ingredient name';

  it('should create an action to add an ingredient', () => {
    const expectedAction = {
      type: types.SUBMIT_INGREDIENT,
      id: 1,
      text: ingredientName,
    };

    expect(actions.submitIngredient(ingredientName))
      .toEqual(expectedAction);
  });
});
