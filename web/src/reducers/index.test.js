/* global expect, it, describe */

import { initialState, reducer } from '.';
import types from '../constants/';

describe('Reducer', () => {
  const ingredient = {
    name: 'name',
    measure: 'measure'
  };

  it('should return the initial state when no action passed', () => {
    expect(reducer(undefined, {}))
      .toEqual(initialState);
  });

  describe('Submit ingredient', () => {
    it('should return the correct state', () => {
      const action = {
        type: types.SUBMIT_INGREDIENT,
        ingredient,
      };

      const expectedState = {
        ingredients: [
          ingredient,
        ],
      };

      expect(reducer(undefined, action))
        .toEqual(expectedState);
    });
  });
});
