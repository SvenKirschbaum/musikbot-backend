import React from 'react';
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Demo from './views/Demo';
import BaseLayout from './components/BaseLayout';
import NoMatch from './components/NoMatch';

function AppRouter() {
  return (
    <Router>
        <Switch>
            <Route path="/demo" component={Demo} />
            <Route component={NoMatch} />
        </Switch>
    </Router>
  );
}

export default AppRouter;
