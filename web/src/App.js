import React from 'react';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import AddIngredient from './component/AddIngredient';
import theme from './theme';

export const App = () => (
  <MuiThemeProvider theme={theme}>
    <div>
      <h1>Reciplease</h1>
      <AddIngredient/>
    </div>
  </MuiThemeProvider>
);

App.propTypes = {};

export default App;
