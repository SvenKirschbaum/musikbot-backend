import React, { Component } from 'react';
import CookieConsent from 'react-cookie-consent';

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
                <span>Hier kommt der Login hin</span>

                <span>Statistik</span>
        
                <span>
                    <a href="https://datenschutz.elite12.de/">Impressum/Disclaimer/Datenschutz</a>

                    <Clock className="clock"></Clock>
                </span>
            </footer>
        </div>
    );
}

export default BaseLayout;
