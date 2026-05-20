---
version: alpha
name: Together AI-design-analysis
description: An inspired interpretation of Together AI's design language — an AI infrastructure
  platform whose surface alternates between near-black hero bands (with a three-color
  orange-magenta-periwinkle gradient as the single piece of brand chrome) and bright
  white research / pricing / docs bands, knit together by a custom display sans and
  an uppercase mono eyebrow face.
colors:
  primary: '#000000'
  on-primary: '#ffffff'
  ink: '#000000'
  body: '#959494'
  hairline: '#959494'
  canvas: '#ffffff'
  canvas-dark: '#010120'
  surface-dark-soft: '#313641'
  on-dark: '#ffffff'
  accent-orange: '#fc4c02'
  accent-magenta: '#ef2cc1'
  accent-periwinkle: '#bdbbff'
  accent-mint: '#c8f6f9'
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1b1b1b'
  surface-container: '#1f1f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353535'
  on-surface: '#e2e2e2'
  on-surface-variant: '#cfc4c5'
  inverse-surface: '#e2e2e2'
  inverse-on-surface: '#303030'
  outline: '#988e90'
  outline-variant: '#4c4546'
  surface-tint: '#c6c6c6'
  primary-container: '#000000'
  on-primary-container: '#757575'
  inverse-primary: '#5e5e5e'
  secondary: '#c3c3ec'
  on-secondary: '#2c2d4e'
  secondary-container: '#454668'
  on-secondary-container: '#b4b5de'
  tertiary: '#c6c6c6'
  on-tertiary: '#303030'
  tertiary-container: '#000000'
  on-tertiary-container: '#757575'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e2e2e2'
  primary-fixed-dim: '#c6c6c6'
  on-primary-fixed: '#1b1b1b'
  on-primary-fixed-variant: '#474747'
  secondary-fixed: '#e1e0ff'
  secondary-fixed-dim: '#c3c3ec'
  on-secondary-fixed: '#171838'
  on-secondary-fixed-variant: '#424466'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c6'
  on-tertiary-fixed: '#1b1b1b'
  on-tertiary-fixed-variant: '#474747'
  background: '#131313'
  on-background: '#e2e2e2'
  surface-variant: '#353535'
  body-gray: '#959494'
typography:
  display-xxl:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 64px
    fontWeight: 500
    lineHeight: 70.4px
    letterSpacing: -1.92px
  display-xl:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 40px
    fontWeight: 500
    lineHeight: 48px
    letterSpacing: -0.8px
  display-lg:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 28px
    fontWeight: 500
    lineHeight: 32.2px
    letterSpacing: -0.42px
  display-md:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 22px
    fontWeight: 500
    lineHeight: 25.3px
    letterSpacing: -0.22px
  body-lg:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 18px
    fontWeight: 400
    lineHeight: 23.4px
    letterSpacing: -0.18px
  body-lg-strong:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 18px
    fontWeight: 500
    lineHeight: 23.4px
    letterSpacing: -0.18px
  body-md:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 16px
    fontWeight: 400
    lineHeight: 20.8px
    letterSpacing: -0.16px
  body-md-strong:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 16px
    fontWeight: 500
    lineHeight: 20.8px
    letterSpacing: -0.16px
  caption:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 14px
    fontWeight: 400
    lineHeight: 19.6px
  caption-strong:
    fontFamily: The Future, Inter, Helvetica Neue, Arial, sans-serif
    fontSize: 14px
    fontWeight: 500
    lineHeight: 19.6px
  mono-caps-button:
    fontFamily: PP Neue Montreal Mono, ui-monospace, SF Mono, Menlo, monospace
    fontSize: 16px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.08px
  mono-caps-eyebrow:
    fontFamily: PP Neue Montreal Mono, ui-monospace, SF Mono, Menlo, monospace
    fontSize: 11px
    fontWeight: 500
    lineHeight: 11px
    letterSpacing: 0.55px
  mono-caps-label:
    fontFamily: PP Neue Montreal Mono, ui-monospace, SF Mono, Menlo, monospace
    fontSize: 11px
    fontWeight: 500
    lineHeight: 15.4px
    letterSpacing: 0.055px
  mono-caption:
    fontFamily: PP Neue Montreal Mono, ui-monospace, SF Mono, Menlo, monospace
    fontSize: 10px
    fontWeight: 400
    lineHeight: 14px
    letterSpacing: 0.05px
rounded:
  none: 0px
  xs: 3.25px
  sm: 4px
  md: 8px
  full: 9999px
  DEFAULT: 0.5rem
  lg: 1rem
  xl: 1.5rem
spacing:
  xxs: 2px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  2xl: 24px
  3xl: 32px
  4xl: 44px
  5xl: 48px
  6xl: 55.2px
  section: 80px
components:
  nav-bar:
    backgroundColor: '{colors.canvas-dark}'
    textColor: '{colors.on-dark}'
    typography: '{typography.body-md}'
    padding: '{spacing.lg} {spacing.3xl}'
  nav-link:
    textColor: '{colors.on-dark}'
    typography: '{typography.body-md}'
  button-primary:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xs} {spacing.2xl}'
  button-secondary-mint:
    backgroundColor: '{colors.accent-mint}'
    textColor: '{colors.ink}'
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xs} {spacing.2xl}'
  button-secondary-white:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xs} {spacing.2xl}'
  button-ghost-on-dark:
    backgroundColor: '{colors.surface-dark-soft}'
    textColor: '{colors.on-dark}'
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.sm}'
  button-outline:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: rgba(0, 0, 0, 0.08)
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.xs}'
  button-icon-circular:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.on-primary}'
    rounded: '{rounded.full}'
  text-input:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: rgba(0, 0, 0, 0.08)
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
  badge-neutral:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: rgba(0, 0, 0, 0.08)
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xxs} {spacing.sm}'
  badge-subtle-on-dark:
    backgroundColor: '{colors.surface-dark-soft}'
    textColor: '{colors.on-dark}'
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xxs} {spacing.sm}'
  hero-band-dark:
    backgroundColor: '{colors.canvas-dark}'
    textColor: '{colors.on-dark}'
    typography: '{typography.display-xxl}'
    padding: '{spacing.section} {spacing.3xl}'
  research-band-dark:
    backgroundColor: '{colors.canvas-dark}'
    textColor: '{colors.on-dark}'
    typography: '{typography.display-xl}'
    padding: '{spacing.section} {spacing.3xl}'
  feature-tab-pill:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md-strong}'
    rounded: '{rounded.sm}'
    padding: '{spacing.md} {spacing.2xl}'
  pricing-sub-tab:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md}'
    rounded: '{rounded.xs}'
    padding: '{spacing.sm} {spacing.lg}'
  stats-card-tinted:
    backgroundColor: '{colors.accent-mint}'
    textColor: '{colors.ink}'
    typography: '{typography.display-xl}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  research-card:
    backgroundColor: '{colors.canvas-dark}'
    textColor: '{colors.on-dark}'
    borderColor: rgba(255, 255, 255, 0.12)
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  testimonial-card:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  article-card:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.display-md}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  code-editor-mockup:
    backgroundColor: '{colors.canvas-dark}'
    textColor: '{colors.on-dark}'
    typography: '{typography.mono-caption}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  data-table-row:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: rgba(0, 0, 0, 0.08)
    typography: '{typography.body-md}'
    padding: '{spacing.md} {spacing.lg}'
  data-table-header:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.body}'
    typography: '{typography.mono-caps-eyebrow}'
    padding: '{spacing.md} {spacing.lg}'
  toggle-pill-group:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.mono-caps-button}'
    rounded: '{rounded.sm}'
    padding: '{spacing.xs}'
  footer:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    typography: '{typography.body-md}'
    padding: '{spacing.section} {spacing.3xl}'
  footer-wordmark-banner:
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.body}'
    typography: '{typography.display-xxl}'
  ex-pricing-tier:
    description: Default Pricing tier card. Mirrors article-card chrome on canvas-soft
      surface with a hairline border.
    backgroundColor: '{colors.canvas}'
    textColor: '{colors.ink}'
    borderColor: rgba(0, 0, 0, 0.08)
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-pricing-tier-featured:
    description: Featured tier — polarity-flipped to canvas-dark with white text.
    backgroundColor: '{colors.ink}'
    textColor: '{colors.on-primary}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-product-selector:
    description: What's Included summary card.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  ex-cart-drawer:
    description: Subscription summary — line items per add-on.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
    item-divider: '{colors.hairline}'
  ex-app-shell-row:
    description: Sidebar nav row. Active state uses brand primary as a left-edge indicator
      bar.
    backgroundColor: '{colors.canvas}'
    activeIndicator: '{colors.primary}'
    rounded: '{rounded.sm}'
    padding: '{spacing.md} {spacing.lg}'
  ex-data-table-cell:
    description: Default data table cell.
    headerBackground: '{colors.hairline}'
    headerTypography: '{typography.mono-caps-eyebrow}'
    bodyTypography: '{typography.body-md}'
    cellPadding: '{spacing.md} {spacing.lg}'
    rowBorder: '{colors.hairline}'
  ex-auth-form-card:
    description: Sign-in / sign-up card.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-modal-card:
    description: Modal dialog surface.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-empty-state-card:
    description: Empty-state illustration frame.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.5xl}'
    captionTypography: '{typography.body-md}'
  ex-toast:
    description: Toast notification surface.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.md} {spacing.lg}'
    typography: '{typography.body-md}'
---

