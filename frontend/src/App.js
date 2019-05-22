import React, {Component} from 'react';
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Songs from './views/Songs';
import NoMatch from './components/NoMatch';
import BaseLayout from './components/BaseLayout';
import Home from './views/Home';
import Archiv from './views/Archiv';
import Token from './views/Token';
import Debug from './views/Debug';
import Log from './views/Log';
import Gapcloser from './views/Gapcloser';
import Stats from './views/Stats';
import Playlist from './views/Playlist';
import UserPage from './views/UserPage';
import AuthenticationContext from './components/AuthenticationContext';


class AppRouter extends Component {

    constructor(props) {
        super(props);
        let loadstate = JSON.parse(localStorage.getItem('loginstate'));
        if(loadstate) {
            this.state = {
                loggedin: loadstate.loggedin,
                user: {},
                token: loadstate.token
            };
        }
        else {
            this.state = {
                loggedin: false,
                user: {},
                token: null
            };
        }

        this.login = this.login.bind(this);
        this.logout = this.logout.bind(this);
        this.loadUser = this.loadUser.bind(this);
    }

    componentDidMount() {
        if(this.state.loggedin) this.loadUser();
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
                            token: response.token
                        });
                        localStorage.setItem('loginstate', JSON.stringify({
                            loggedin: true,
                            token: response.token
                        }));
                        this.loadUser();
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
            user: {},
            token: null
        });
        localStorage.removeItem('loginstate');
    }

    loadUser() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        headers.append("Authorization", "Bearer " + this.state.token);
        fetch("/api/v2/user/self", {
            method: 'GET',
            headers: headers
        })
        .then((res) => res.json())
        .then((res) => {
            this.setState({
                user: res
            });
        })
        .catch((res) => {
           console.error("Error loading user" + res);
        });
    }

    render() {
        return (
            <Router>
                <AuthenticationContext.Provider value={{ ...this.state, login: this.login, logout: this.logout, reload: this.loadUser}}>
                    <BaseLayout>
                        <Switch>
                            <Route path="/" exact component={Home} />
                            <Route path="/songs" component={Songs} />
                            <Route path="/archiv/:page?" component={Archiv} />
                            <Route path="/token" component={Token} />
                            <Route path="/debug" component={Debug} />
                            <Route path="/log" component={Log} />
                            <Route path="/gapcloser" component={Gapcloser} />
                            <Route path="/statistik" component={Stats} />
                            <Route path="/import" component={Playlist} />
                            <Route path="/users/:name" component={UserPage} />
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
