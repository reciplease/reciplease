/* global expect, it, describe, jest, beforeEach */

import React from 'react';
import { mount, shallow } from 'enzyme';
import fetchMock from 'fetch-mock';
import AddIngredient from './AddIngredient';
import { Button, Select } from '@material-ui/core';

describe('AddIngredient component', () => {
  let component;
  fetchMock.mock('api/measures', ['measure1', 'measure2']);

  let ingredient = {
    id: 'ABC',
    name: 'ingredient name',
    measure: 'measure1'
  };

  fetchMock.mock('api/ingredients', ingredient, { method: 'POST', name: 'INGREDIENT-POST'});

  beforeEach(() => {
    component = shallow(
      <AddIngredient/>,
    );
  });

  it('should render successfully', () => {
    expect(component.exists()).toEqual(true);
  });
  it('should have name input', () => {
    expect(component.find('.ingredient-name-input').length).toEqual(1);
  });
  it('should change state when there is text input', () => {
    let nameInput = component.find('.ingredient-name-input');
    nameInput.prop('onChange')({ target: { value: 'name' } });
    component.update();
    expect(component.state('name'))
      .toEqual('name');
  });
  it('should have measure select', () => {
    expect(component.find(Select).length)
      .toEqual(1);
  });
  it('should change state when select is changed', () => {
    let measureSelect = component.find(Select);
    measureSelect.prop('onChange')({ target: { value: 'measure' } });
    component.update();
    expect(component.state('measure'))
      .toEqual('measure');
  });
  describe('Add ingredient button', () => {
    it('should exist', () => {
      expect(component.find(Button).length).toEqual(1);
    });
    it('should call function when clicked', () => {
      component = mount(<AddIngredient />);
      expect(fetchMock.lastCall('INGREDIENT-POST')).toBeUndefined();

      component.find('form').simulate('submit');

      console.log(fetchMock.lastCall(true));
      expect(fetchMock.lastCall('INGREDIENT-POST')).not.toBeUndefined();
    });
    it('should call function with ingredient object', () => {
      component = mount(<AddIngredient />);
      component.setState({
        name: 'name',
        measure: 'measure'
      });

      component.find('form')
        .simulate('submit');

      expect(fetchMock.lastCall('INGREDIENT-POST')).not.toBeUndefined();
      expect(fetchMock.lastCall('INGREDIENT-POST')[1]['body']).not.toBeUndefined();
      expect(fetchMock.lastCall('INGREDIENT-POST')[1]['body']).toBe(JSON.stringify({name: 'name', measure: 'measure'}));
    });
  });
});
