/* global expect, it, describe */

import types from '../constants/';
import actions from '.';

describe('Actions', () => {
  const ingredient = 'ingredient name';

  it('should create an action to add an ingredient', () => {
    const expectedAction = {
      type: types.SUBMIT_INGREDIENT,
      ingredient: ingredient,
    };

    expect(actions.submitIngredient(ingredient))
      .toEqual(expectedAction);
  });
});
