import React, {Component} from 'react';
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Demo from './views/Demo';
import NoMatch from './components/NoMatch';
import BaseLayout from './components/BaseLayout';
import Home from './views/Home';
import AuthenticationContext from './components/AuthenticationContext';


class AppRouter extends Component {

    constructor(props) {
        super(props);
        let loadstate = JSON.parse(localStorage.getItem('loginstate'));
        if(loadstate) {
            this.state = {
                loggedin: loadstate.loggedin,
                user: loadstate.user,
                token: loadstate.token
            };
        }
        else {
            this.state = {
                loggedin: false,
                user: null,
                token: null
            };
        }

        this.login = this.login.bind(this);
        this.logout = this.logout.bind(this);


        this.state.login = this.login;
        this.state.logout = this.logout;
    }

    login(username, password) {
        return new Promise((resolve,reject) => {
            let headers = new Headers();
            headers.append("Content-Type", "application/json");
            fetch("/api/v2/login", {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    'username': username,
                    'password': password
                })
            })
                .then(res => res.json())
                .then(response => {
                    if(response.success) {
                        this.setState({
                            loggedin: true,
                            user: response.user,
                            token: response.token
                        });
                        localStorage.setItem('loginstate', JSON.stringify({
                            loggedin: true,
                            token: response.token,
                            user: response.user
                        }));
                        resolve();
                    }
                    else {
                        reject(response.error);
                    }
                });
        });
    }

    logout() {
        this.loggedin = false;
        this.user = null;
        this.setState({
            loggedin: false,
            user: null,
            token: null
        });
        localStorage.removeItem('loginstate');
    }

    render() {
        return (
            <Router basename="/v2">
                <AuthenticationContext.Provider value={this.state}>
                    <BaseLayout>
                        <Switch>
                            <Route path="/" exact component={Home} />
                            <Route path="/demo" component={Demo} />
                            <Route component={NoMatch} />
                        </Switch>
                    </BaseLayout>
                </AuthenticationContext.Provider>
            </Router>
        );
    }
}
export {AuthenticationContext as AuthState};
export default AppRouter;
