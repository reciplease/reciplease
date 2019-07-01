import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';

class AddIngredient extends Component {
  constructor(props) {
    super(props);
    this.state = {
      name: '',
      measure: 'default',
      measures: []
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
          .map(measure => <option value={measure}>{measure}</option>);
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
    this.props.submitIngredient({
        name: this.state.name,
        measure: this.state.measure
    });
    this.setState({
      name: '',
      measure: 'default'
    });
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <div>
            <label>
              Name:
              <input
                className="ingredient-name-input"
                value={this.state.name}
                onChange={this.handleInputChange}
              />
            </label>
          </div>
          <div>
            <label>
              Measure:
              <select
                className="ingredient-measure-select"
                value={this.state.measure}
                onChange={this.handleSelectChange}
              >
                <option value="default" disabled>Select...</option>
                {this.state.measures}
              </select>
            </label>
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
  submitIngredient: PropTypes.func.isRequired,
};

export default AddIngredient;
