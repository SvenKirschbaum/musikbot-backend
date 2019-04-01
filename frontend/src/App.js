import React from 'react';
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Demo from './views/Demo';
import BaseLayout from './components/BaseLayout';

function AppRouter() {
  return (
    <Router>
        <Switch>
            <Route exact path="/" component={BaseLayout} />
            <Route path="/demo" component={Demo} />
        </Switch>
    </Router>
  );
}

export default AppRouter;
