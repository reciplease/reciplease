import { createMuiTheme } from '@material-ui/core/styles';
import indigo from '@material-ui/core/colors/indigo';
import pink from '@material-ui/core/colors/pink';

export default createMuiTheme({
  palette: {
    type: 'light',
    primary: pink,
    secondary: indigo
  }
});
