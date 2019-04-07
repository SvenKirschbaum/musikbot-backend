import React, {Component} from 'react';

class DragFixedCell extends Component {

    getSnapshotBeforeUpdate(prevProps) {
        if (!this.ref) {
            return null;
        }

        const isDragStarting =
            this.props.isDragOccurring && !prevProps.isDragOccurring;

        if (!isDragStarting) {
            return null;
        }

        const { width, height } = this.ref.getBoundingClientRect();

        const snapshot = {
            width,
            height,
        };

        return snapshot;
    }

    componentDidUpdate(
        prevProps,
        prevState,
        snapshot,
    ) {
        const ref = this.ref;
        if (!ref) {
            return;
        }

        if (snapshot) {
            if (ref.style.width === snapshot.width) {
                return;
            }
            ref.style.width = `${snapshot.width}px`;
            ref.style.height = `${snapshot.height}px`;
            return;
        }

        if (this.props.isDragOccurring) {
            return;
        }

        // inline styles not applied
        if (ref.style.width == null) {
            return;
        }

        // no snapshot and drag is finished - clear the inline styles
        ref.style.removeProperty('height');
        ref.style.removeProperty('width');
    }

    setRef = (ref) => {
        this.ref = ref;
    };

    render() {
        return <td {...this.props.addToElem} className={this.props.className} onClick={this.props.onClick} ref={this.setRef}>{this.props.children}</td>;
    }
}
export default DragFixedCell;