import React, { Component } from 'react';

class Clock extends Component {
  constructor(props) {
    super(props);
    this.state = {date: new Date()};
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => {
        this.setState({
          date: new Date()
        });
      },
      1000
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  render() {
    return (
        <span className={ this.props.className }>{this.state.date.toLocaleTimeString("de", {hour: 'numeric', minute: 'numeric'})}</span>
    );
  }
}

export default Clock;