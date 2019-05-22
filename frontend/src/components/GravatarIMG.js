import React, { Component } from 'react';
import ReactCSSTransitionReplace from 'react-css-transition-replace';

class GravatarIMG extends Component {

    render() {
        let animate = this.props.animate;
        if(animate === undefined) animate = false;
        if(animate)
            return (
                <ReactCSSTransitionReplace transitionName="cross-fade">
                    <img key={this.props.children} width={(this.props.size === undefined ? "20" : this.props.size)} height={(this.props.size === undefined ? "20" : this.props.size)} alt="profilbild" src={"https://www.gravatar.com/avatar/" + this.props.children + "?s="+(this.props.size === undefined ? "20" : this.props.size)+"&d="+encodeURIComponent("https://musikbot.elite12.de/img/favicon.png")}></img>
                </ReactCSSTransitionReplace>
            );
        else
            return (
                <img key={this.props.children} width={(this.props.size === undefined ? "20" : this.props.size)} height={(this.props.size === undefined ? "20" : this.props.size)} alt="profilbild" src={"https://www.gravatar.com/avatar/" + this.props.children + "?s="+(this.props.size === undefined ? "20" : this.props.size)+"&d="+encodeURIComponent("https://musikbot.elite12.de/img/favicon.png")}></img>
            );
    }
}

export default GravatarIMG;