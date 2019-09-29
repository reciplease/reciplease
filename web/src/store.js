import { combineReducers, createStore } from 'redux';
import reducer from './reducers';

const reducers = combineReducers({
  reducer,
});

export default createStore(reducers);
