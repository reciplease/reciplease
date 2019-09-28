/* global expect, it, describe, jest, beforeEach */

import React from 'react';
import { mount, shallow } from 'enzyme';
import fetchMock from 'fetch-mock';
import AddIngredient from './AddIngredient';
import { Button, MenuItem, Select } from '@material-ui/core';

describe('AddIngredient component', () => {
  let component;
  let submitMock;
  fetchMock.mock('api/profile/ingredients', {alps: {descriptor: [{descriptor: [{doc: {value: "measure1, measure2"}}]}]}});
  fetchMock.mock('api/ingredients', {});

  beforeEach(() => {
    submitMock = jest.fn();
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
      component = mount(<AddIngredient submitIngredient={submitMock} />);
      expect(submitMock.mock.calls.length).toEqual(0);

      component.find('form').simulate('submit');

      expect(submitMock.mock.calls.length).toEqual(0);
    });
    it('should call function with ingredient object', () => {
      component = mount(<AddIngredient submitIngredient={submitMock}/>);
      component.setState({
        name: 'name',
        measure: 'measure'
      });

      component.find('form')
        .simulate('submit');

      expect(submitMock.mock.calls[0][0])
        .toEqual({
            name: 'name',
            measure: 'measure'
        });
    });
  });
});
