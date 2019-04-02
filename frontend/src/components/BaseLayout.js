import React, { Component } from 'react';
import { Link } from "react-router-dom";
import CookieConsent from 'react-cookie-consent';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Spinner from 'react-bootstrap/Spinner'

import LoginService from '../services/LoginService.js';

import Clock from './Clock.js';

import reactlogo from '../res/react.png';
import spotifylogo from '../res/spotify.svg';

class BaseLayout extends Component {
    render() {
        return (
            <div>
                <CookieConsent
                    location="top"
                >
                    This website uses cookies to ensure you get the best experience on our website. <a className="cookielink" href="https://cookiesandyou.com/">Learn more</a>
                </CookieConsent>
                {this.props.children}
                <Footer></Footer>
            </div>
        );
    }
}

function Footer () {
    return (
        <div>
            <img className="spotify-logo" alt="spotify Logo" src={spotifylogo} />
            <img className="react-logo" alt="HTML5 Logo" src={reactlogo} />
            <footer className="d-flex flex-row justify-content-between">
                
                <LoginFooter></LoginFooter>

                <Link to="/statistik">Statistik</Link>
        
                <span>
                    <a href="https://datenschutz.elite12.de/">Impressum/Disclaimer/Datenschutz</a>

                    <Clock className="clock"></Clock>
                </span>
            </footer>
        </div>
    );
}

class LoginFooter extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isPaneOpen: false
        };
    }

    render() {
        if(LoginService.isLoggedIn()) {
            return (
                <span className="LoginFooter">
                    <img alt="profilbild" src={LoginService.getPicture()}></img>
                    <span>Wilkommen <Link to={`/users/${LoginService.getName()}`}>{LoginService.getName()}</Link></span>
                    <Link to="#" onClick={() => {LoginService.logout()}}>(Logout)</Link>
                    <span>Menü</span>
                </span>
            );
        }
        else {
            return (
                <span className="LoginFooter">
                    {this.state.isPaneOpen &&
                        <LoginBox onClose={() => this.setState({ isPaneOpen: false })}></LoginBox>
                    }
                    <Link to="#" onClick={() => this.setState({ isPaneOpen: true })}>Login</Link>
                    <Link to="/register">Registrieren</Link>
                </span>
            );
        }
    }
}

class LoginBox extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loading: false,
            username: '',
            password: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }
    handleSubmit(e) {
        this.setState({loading: true}, () => {
            //TODO:
            let result = LoginService.login(this.state.username,this.state.password);
            if(result) this.props.onClose();
        });
        e.preventDefault();
    }

    handleChange(event) {
        const target = event.target;
        const value = target.value;
        const name = target.name;

        this.setState({
            [name]: value
        });
    }
    render() {
        return (
            <div className="LoginBox">
                <button type="button" className="close" onClick={this.props.onClose}>
                    <span>&times;</span>
                </button>
                <Form onSubmit={this.handleSubmit}>
                    <Form.Group as={Form.Row} controlId="username">
                        <Form.Label>Username</Form.Label>
                        <Form.Control type="text" name="username" value={this.state.username} onChange={this.handleChange} disabled={this.state.loading} />
                    </Form.Group>
                    <Form.Group as={Form.Row} controlId="password">
                        <Form.Label>Passwort</Form.Label>
                        <Form.Control type="password" name="password" value={this.state.password} onChange={this.handleChange} disabled={this.state.loading} />
                    </Form.Group>
                    <Button variant="light" type="submit" className="float-right" size="sm" disabled={this.state.loading}>
                        {this.state.loading &&
                            <Spinner
                                as="span"
                                animation="border"
                                size="sm"
                                className="LoginSpinner"
                            />
                        }
                        Einloggen
                    </Button>
                </Form>
            </div>
        );
    }
}

export default BaseLayout;
