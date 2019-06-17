/* global expect, it, describe */

import { initialState, reducer } from '.';
import types from '../constants/';

describe('Reducer', () => {
  const ingredientName = 'ingredient name';

  it('should return the initial state when no action passed', () => {
    expect(reducer(undefined, {}))
      .toEqual(initialState);
  });

  describe('Submit ingredient', () => {
    it('should return the correct state', () => {
      const action = {
        type: types.SUBMIT_INGREDIENT,
        id: 1,
        text: ingredientName,
      };

      const expectedState = {
        ingredients: [
          {
            id: 1,
            text: ingredientName,
          },
        ],
      };

      expect(reducer(undefined, action))
        .toEqual(expectedState);
    });
  });
});