// eslint-disable-next-line max-classes-per-file
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { getTranslation } from '../../client/OpenAudioAppContainer';
import { showTextModal } from '../modal/InputModal';
import { setGlobalState } from '../../state/store';

export const setTab = (tab) => {
  setGlobalState({
    currentTab: tab,
  });
};

class TabWindow extends Component {
  constructor(props) {
    super(props);
    this.openUpgradeDialog = this.openUpgradeDialog.bind(this);
  }

  openUpgradeDialog() {
    showTextModal(
      'Important platform update',
      "OpenAudioMc moved to a new platform which will entirely replace the old one. Please check our full changelog on Spigot and update at your earliest convenience. If you have any questions, please contact us on Discord. Support for the legacy 'Craftmend' platform will be dropped this summer.",
      '',
      "<a href='https://www.spigotmc.org/resources/openaudiomc-proximity-voice-chat-and-music-without-mods.30691/update?update=498012'>Visit changelog on Spigot</a>",
      '<i>this message is only applicable to server owners</i>',
    );
  }

  render() {
    let pages = React.Children.map(this.props.children, (child) => ({
      name: child.props.name,
      content: child.props.content,
      hidden: child.props.hidden,
      buttonContent: child.props.buttonContent,
    }));

    // remove hidden pages
    pages = pages.filter((page) => !page.hidden);
    let pageIndex = this.props.currentTab;

    // move active page back if it's out of bounds
    if (pageIndex >= pages.length) {
      pageIndex = pages.length - 1;
    }

    let pill = <div className="small-pill free">Free</div>;
    if (this.props.isPremium) pill = <div className="small-pill premium">Premium</div>;

    // placeholder for player uuid
    let playerUuid = '00000000-0000-0000-0000-000000000000';
    if (this.props.currentUser) playerUuid = this.props.currentUser.uuid;

    if (!this.props.navbarDetails) pill = '';

    let navbarButtons = pages.map((page, index) => (
      <span className="tab" key={page.name}>
        <button
          className={`${index === this.props.currentTab ? 'active main-header-link' : 'inactive main-header-link'} text-white border-solid border-2 border-gray-800 p-4 h-auto flex items-center justify-center rounding-top`}
          onClick={() => setTab(index)}
          type={page.buttonContent ? 'button' : 'submit'}
        >
          {page.buttonContent ? page.buttonContent : null}
          {page.buttonContent ? <span className="mr-2 ml-2 hiddennp lg:block">{page.name}</span> : null}
        </button>
      </span>
    ));

    // is there only one page? then hide the navbar
    if (navbarButtons.length === 1) navbarButtons = '';

    // only show navbar when we're unlocked
    const showNavbar = !this.props.clickLock;

    return (
      <div className="main-container tabbed">
        {showNavbar ? (
          <div className="main-header flex justify-start">
            <span className="theme-color-text md:pl-10 w-1/3">
              <div
                className="rounding-bottom rounding-top px-1 py-1 flex items-center justify-start hidden-on-mobile"
              >
                {this.props.navbarDetails ? (
                  <img
                    src={`https://visage.surgeplay.com/face/512/${playerUuid}`}
                    className="rounding-top rounding-bottom inline mr-5 w-9 h-9"
                    alt="avatar"
                  />
                ) : null}
                {getTranslation(null, 'serverName')}
                {pill}
              </div>
            </span>

            <div className="header-menu w-1/3 center flex justify-center">
              {navbarButtons}
            </div>
            <div className="header-notice w-1/3 flex justify-end">
              <a
                className="menu-link-main soft-text"
                id="notice"
                href="https://openaudiomc.net/"
              >
                &copy; OpenAudioMc 2016-2023. All Rights Reserved.
              </a>
            </div>
          </div>
        ) : null}
        <div className="content-wrapper">
          {pages[pageIndex].content}
        </div>
      </div>
    );
  }
}

export default connect(mapStateToProps)(TabWindow);

function mapStateToProps(state) {
  return {
    currentTab: state.currentTab,
    isPremium: state.isPremium,
    currentUser: state.currentUser,
    navbarDetails: state.navbarDetails,
    clickLock: state.clickLock,
  };
}

export class TabPage extends Component {
  render() {
    return this.props.children;
  }
}

// eslint if struggeling with re-assigned props

/* eslint-disable react/no-unused-prop-types */
TabPage.propTypes = {
  name: PropTypes.string.isRequired,
  content: PropTypes.element.isRequired,
  hidden: PropTypes.bool,
  buttonContent: PropTypes.element,
};

TabPage.defaultProps = {
  hidden: false,
  buttonContent: null,
};