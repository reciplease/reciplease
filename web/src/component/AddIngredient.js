import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button, InputLabel, MenuItem, Select } from '@material-ui/core';

class AddIngredient extends Component {
  constructor(props) {
    super(props);
    this.state = {
      name: '',
      measure: 'default',
      measures: [],
      created: false
    };
    this.handleInputChange = this.handleInputChange.bind(this);
    this.handleSelectChange = this.handleSelectChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidMount() {
    fetch('api/profile/ingredients')
      .then(res => res.json())
      .then((result) => {
        const measures = this.getMeasuresFromResult(result)
          .map(measure => <MenuItem value={measure}>{measure}</MenuItem>);
        this.setState({ measures });
      });
  }

  getMeasuresFromResult(result) {
    return result.alps.descriptor[0].descriptor[1].doc.value.split(', ');
  }

  handleInputChange(event) {
    this.setState({ name: event.target.value });
  }

  handleSelectChange(event) {
    this.setState({ measure: event.target.value });
  }

  handleSubmit(event) {
    event.preventDefault();
    fetch('api/ingredients',
      {
        credentials: 'include',
        method: 'post',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: this.state.name,
          measure: this.state.measure
        })
      })
      .then(res => res.json())
      .then(result => {
        console.log(result);
        this.setState({ successful: true });
      })
      .catch(reason => console.log(reason));
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <div>
            <InputLabel>Name</InputLabel>
            <input
              className="ingredient-name-input"
              value={this.state.name}
              onChange={this.handleInputChange}
            />
          </div>
          <div>
            <InputLabel>
              Measure
            </InputLabel>
            <Select
              value={this.state.measure}
              onChange={this.handleSelectChange}
              >
              <MenuItem value="default" disabled>Select...</MenuItem>
              {this.state.measures}
            </Select>
          </div>
          <Button variant="contained" color="primary" type="submit">
            Add Ingredient
          </Button>
        </form>
      </div>
    );
  }
}

AddIngredient.propTypes = {
  measures: PropTypes.arrayOf(PropTypes.string),
  submitIngredient: PropTypes.func.isRequired
};

export default AddIngredient;
