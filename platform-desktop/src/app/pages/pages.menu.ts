export const PAGES_MENU = [
  {
    path: 'pages',
    children: [
      {
        path: 'dashboard',
        data: {
          menu: {
            title: 'Dashboard',
            icon: 'ion-android-home',
            selected: false,
            expanded: false,
            order: 0,
          },
        },
      },

      {
        path: 'documents',  // path for our page
        data: { // custom menu declaration
          menu: {
            title: 'Documents', // menu title
            icon: 'ion-document', // menu icon
            pathMatch: 'prefix', // use it if item children not displayed in menu
            selected: false,
            expanded: false,
            order: 0,
          },
        },
      },

      {
        path: 'profile',  // path for our page
        data: { // custom menu declaration
          menu: {
            title: 'Profile', // menu title
            icon: 'ion-android-person', // menu icon
            pathMatch: 'prefix', // use it if item children not displayed in menu
            selected: false,
            expanded: false,
            order: 0,
          },
        },
      },




    ],
  },
];
