/* global expect, it, describe, jest, beforeEach */

import React from 'react';
import { mount, shallow } from 'enzyme';
import fetchMock from 'fetch-mock';
import AddIngredient from './AddIngredient';

describe('AddIngredient component', () => {
  let component;
  const submitMock = jest.fn();
  fetchMock.mock('api/profile/ingredients', {alps: {descriptor: [{descriptor: [{doc: {value: "measure1, measure2"}}]}]}});

  beforeEach(() => {
    component = shallow(
      <AddIngredient
        submitIngredient={submitMock}
      />,
    );
  });

  it('should render successfully', () => {
    expect(component.exists()).toEqual(true);
  });
  it('should have name input', () => {
    expect(component.find('.ingredient-name-input').length).toEqual(1);
  });
  it('should have measure select', () => {
    expect(component.find('.ingredient-measure-select').length).toEqual(1);
  });
  describe('Add ingredient button', () => {
    it('should exist', () => {
      expect(component.find('.ingredient-submit').length).toEqual(1);
    });
    it('should call function when clicked', () => {
      component = mount(<AddIngredient submitIngredient={submitMock} />);

      expect(submitMock.mock.calls.length).toEqual(0);
      component.find('form').simulate('submit');
      expect(submitMock.mock.calls.length).toEqual(1);
    });
  });
});
