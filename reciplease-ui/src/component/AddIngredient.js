import React, { Component } from 'react';
import PropTypes from 'prop-types';

class AddIngredient extends Component {
  constructor(props) {
    super(props);
    this.state = { name: '' };
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({ name: event.target.value });
  }

  handleSubmit(event) {
    event.preventDefault();
    this.props.submitIngredient(this.state.name);
    this.setState({ name: '' });
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <label>
            Name:
            <input
              className="ingredient-name-input"
              value={this.state.name}
              onChange={this.handleChange}
            />
          </label>
          <button type="submit" className="ingredient-submit">
            Add Ingredient
          </button>
        </form>
      </div>
    );
  }
}

AddIngredient.propTypes = {
  submitIngredient: PropTypes.func.isRequired,
};

export default AddIngredient;
