import React, { Component } from 'react';

class GravatarIMG extends Component {

    render() {
        return (
            <img alt="profilbild" src={"https://www.gravatar.com/avatar/" + this.props.children + "?s=20&d="+encodeURIComponent("https://musikbot.elite12.de/img/favicon_small.png")}></img>
        );
    }
}

export default GravatarIMG;