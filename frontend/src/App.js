import React from 'react';
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Demo from './views/Demo';
import NoMatch from './components/NoMatch';
import BaseLayout from './components/BaseLayout';
import Home from './views/Home';

function AppRouter() {
  return (
    <Router>
        <BaseLayout>
            <Switch>
                <Route path="/" exact component={Home} />
                <Route path="/demo" component={Demo} />
                <Route component={NoMatch} />
            </Switch>
        </BaseLayout>
    </Router>
  );
}

export default AppRouter;
