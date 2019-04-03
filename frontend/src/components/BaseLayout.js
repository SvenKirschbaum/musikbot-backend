import React, { Component } from 'react';
import { Link } from "react-router-dom";
import CookieConsent from 'react-cookie-consent';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Spinner from 'react-bootstrap/Spinner';
import { CSSTransition } from 'react-transition-group';

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

class Footer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isPaneOpen: false,
            isMenuOpen: false
        };
    }
    render() {
        return (
            <div>
                <img className="spotify-logo" alt="spotify Logo" src={spotifylogo} />
                <img className="react-logo" alt="HTML5 Logo" src={reactlogo} />
                <CSSTransition
                    classNames="slideright"
                    timeout={300}
                    unmountOnExit
                    in={this.state.isPaneOpen}>
                        <LoginBox onClose={() => this.setState({ isPaneOpen: false })}></LoginBox>
                </CSSTransition>
                <CSSTransition
                    classNames="slideup"
                    timeout={300}
                    unmountOnExit
                    in={this.state.isMenuOpen}>
                    <AMenu onItemClick={() => this.setState({ isMenuOpen: false })}></AMenu>
                </CSSTransition>
                <footer className="d-flex flex-row justify-content-between">
                    
                    <LoginFooter onLogin={() => this.setState({isPaneOpen: true})} onMenu={() => this.setState({isMenuOpen: !this.state.isMenuOpen})}></LoginFooter>

                    <Link to="/statistik">Statistik</Link>
            
                    <span>
                        <a href="https://datenschutz.elite12.de/">Impressum/Disclaimer/Datenschutz</a>

                        <Clock className="clock"></Clock>
                    </span>
                </footer>
            </div>
        );
    }
}

function LoginFooter(props) {
    if(LoginService.isLoggedIn()) {
        return (
            <span className="LoginFooter">
                <img alt="profilbild" src={LoginService.getPicture()}></img>
                <span>Willkommen <Link to={`/users/${LoginService.getName()}`}>{LoginService.getName()}</Link></span>
                <Link to="#" onClick={() => {LoginService.logout()}}>(Logout)</Link>
                <Link to="#" onClick={props.onMenu}>Menü</Link>
            </span>
        );
    }
    else {
        return (
            <span className="LoginFooter">
                <Link to="#" onClick={props.onLogin}>Login</Link>
                <Link to="/register">Registrieren</Link>
            </span>
        );
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
            //TODO: Handle Errors after LoginService implementation
            LoginService.login(this.state.username,this.state.password).then(() => {
                this.props.onClose();
            });
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

function AMenu(props) {
    return (
        <ul className="AMenu">
            <li><Link to="/" onClick={props.onItemClick}>Startseite</Link></li>
			<li><Link to="/archiv" onClick={props.onItemClick}>Archiv</Link></li>
			<li><Link to="/statistik" onClick={props.onItemClick}>Statistik</Link></li>
			<li><Link to="/tokens" onClick={props.onItemClick}>Auth-Token</Link></li>
            { LoginService.isAdmin() && 
                <React.Fragment>
                    <li><Link to="/import" onClick={props.onItemClick}>Playlist Importieren</Link></li>
                    <li><Link to="/songs" onClick={props.onItemClick}>Gesperrte Songs</Link></li>
                    <li><Link to="/gapcloser" onClick={props.onItemClick}>Gapcloser</Link></li>
                    <li><Link to="/log" onClick={props.onItemClick}>Log</Link></li>
                    <li><Link to="/debug" onClick={props.onItemClick}>Entwicklermenü</Link></li>
                </React.Fragment>
            }
        </ul>
    );
}

export default BaseLayout;
