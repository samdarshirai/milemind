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
  surface: '#f9f9f9'
  surface-dim: '#dadada'
  surface-bright: '#f9f9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f3'
  surface-container: '#eeeeee'
  surface-container-high: '#e8e8e8'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1b1b1b'
  on-surface-variant: '#4c4546'
  inverse-surface: '#303030'
  inverse-on-surface: '#f1f1f1'
  outline: '#7e7576'
  outline-variant: '#cfc4c5'
  surface-tint: '#5e5e5e'
  primary-container: '#1b1b1b'
  on-primary-container: '#848484'
  inverse-primary: '#c6c6c6'
  secondary: '#3a6568'
  on-secondary: '#ffffff'
  secondary-container: '#bdebee'
  on-secondary-container: '#406b6e'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1b1b1b'
  on-tertiary-container: '#848484'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2e2e2'
  primary-fixed-dim: '#c6c6c6'
  on-primary-fixed: '#1b1b1b'
  on-primary-fixed-variant: '#474747'
  secondary-fixed: '#bdebee'
  secondary-fixed-dim: '#a2cfd2'
  on-secondary-fixed: '#002022'
  on-secondary-fixed-variant: '#204d50'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c6'
  on-tertiary-fixed: '#1b1b1b'
  on-tertiary-fixed-variant: '#474747'
  background: '#f9f9f9'
  on-background: '#1b1b1b'
  surface-variant: '#e2e2e2'
  body-grey: '#959494'
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
  DEFAULT: 0.25rem
  lg: 0.5rem
  xl: 0.75rem
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
    description: What's Included summary card — repurposed for the brand's GPU / inference
      packaging tiers.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.2xl}'
  ex-cart-drawer:
    description: Subscription summary — line items per add-on (NOT a literal e-commerce
      cart).
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
    description: Mirrors the brand's pricing-page table. Header uses mono-caps-eyebrow
      uppercase; body uses body-md.
    headerBackground: '{colors.hairline}'
    headerTypography: '{typography.mono-caps-eyebrow}'
    bodyTypography: '{typography.body-md}'
    cellPadding: '{spacing.md} {spacing.lg}'
    rowBorder: '{colors.hairline}'
  ex-auth-form-card:
    description: Sign-in / sign-up card. Mirrors article-card chrome with text-input
      primitives inside.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-modal-card:
    description: Modal dialog surface — same chrome as article-card; relies on tinted
      scrim instead of card shadow.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.3xl}'
  ex-empty-state-card:
    description: Empty-state illustration frame. Generous padding on canvas-soft surface.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.5xl}'
    captionTypography: '{typography.body-md}'
  ex-toast:
    description: Toast notification surface — flat-cornered article-card chrome with
      a soft brand-tinted drop shadow.
    backgroundColor: '{colors.canvas}'
    rounded: '{rounded.sm}'
    padding: '{spacing.md} {spacing.lg}'
    typography: '{typography.body-md}'
---

