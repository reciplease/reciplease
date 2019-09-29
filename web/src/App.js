import React from 'react';
import { connect } from 'react-redux';
import AddIngredient from './component/AddIngredient';
import actions from './actions';
import PropTypes from 'prop-types';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import theme from './theme';

export const App = ({ submitIngredient }) => (
  <MuiThemeProvider theme={theme}>
    <div>
      <h1>Reciplease</h1>
      <AddIngredient submitIngredient={submitIngredient}/>
    </div>
  </MuiThemeProvider>
);

App.propTypes = {
  submitIngredient: PropTypes.func.isRequired,
};

const mapStateToProps = state => {
  return state.reducer
};

const mapDispatchToProps = dispatch => ({
  submitIngredient: (ingredient) => {
    if (ingredient) {
      dispatch(actions.submitIngredient(ingredient));
    }
  },
});

export default connect(mapStateToProps, mapDispatchToProps)(App);
