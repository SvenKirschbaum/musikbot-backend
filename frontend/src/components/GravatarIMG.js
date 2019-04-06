import React, { Component } from 'react';

class GravatarIMG extends Component {
    constructor(props) {
        super(props);
        this.state = {
            gravatarurl: "https://www.gravatar.com/avatar/" + props.children + "?s=20&d=musikbot.elite12.de/img/favicon_small.png"
        };
    }

    render() {
        return (
            <img alt="profilbild" src={this.state.gravatarurl}></img>
        );
    }
}

export default GravatarIMG;