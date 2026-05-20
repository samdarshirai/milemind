---
name: Milemind Velocity
colors:
  surface: '#fbf9f8'
  surface-dim: '#dbd9d9'
  surface-bright: '#fbf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#f0eded'
  surface-container-high: '#eae8e7'
  surface-container-highest: '#e4e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#4c4546'
  inverse-surface: '#303031'
  inverse-on-surface: '#f2f0f0'
  outline: '#7e7576'
  outline-variant: '#cfc4c5'
  surface-tint: '#5e5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1b1b1b'
  on-primary-container: '#848484'
  inverse-primary: '#c6c6c6'
  secondary: '#3a6568'
  on-secondary: '#ffffff'
  secondary-container: '#bdebee'
  on-secondary-container: '#406b6e'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#171838'
  on-tertiary-container: '#8081a7'
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
  tertiary-fixed: '#e1e0ff'
  tertiary-fixed-dim: '#c3c3ec'
  on-tertiary-fixed: '#171838'
  on-tertiary-fixed-variant: '#424466'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e2'
  mint-accent: '#c8f6f9'
  deep-canvas: '#010120'
  surface-soft: '#313641'
  energy-orange: '#fc4c02'
  vibe-magenta: '#ef2cc1'
  data-periwinkle: '#bdbbff'
  hairline-light: rgba(0, 0, 0, 0.08)
  hairline-dark: rgba(255, 255, 255, 0.12)
typography:
  display-xl:
    fontFamily: Sora
    fontSize: 40px
    fontWeight: '500'
    lineHeight: 48px
    letterSpacing: -0.8px
  display-lg:
    fontFamily: Sora
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 32px
    letterSpacing: -0.4px
  display-md:
    fontFamily: Sora
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 26px
    letterSpacing: -0.2px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.18px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 21px
    letterSpacing: -0.16px
  button-mono:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.08px
  eyebrow-mono:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 11px
    letterSpacing: 0.55px
  caption-mono:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '400'
    lineHeight: 14px
    letterSpacing: 0.05px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  2xl: 24px
  3xl: 32px
  section-gap: 48px
---

## Brand & Style
The design system for Milemind is rooted in **Technical Minimalism** and **High-Contrast Performance**. It targets serious runners and data-focused athletes who value precision, research-driven insights, and clarity under effort. The aesthetic is "Engineering-Chic"—balancing the raw, functional look of a laboratory with the sleek polish of high-end athletic gear.

The visual narrative is defined by "Polarity Flips," using alternating bands of deep ink and pure canvas to create rhythm without decorative clutter. The emotional response should be one of focus, urgency, and authoritative reliability.

**Design Style: High-Contrast / Bold**
- Heavy reliance on pure black (`#000000`) and white (`#FFFFFF`) for immediate legibility.
- Monospaced typography utilized as a functional tool for data and interaction.
- Vibrancy is introduced sparingly through a signature "Mint" interactive accent and a brand-specific gradient to signify momentum and progress.

## Colors
The palette is architectural, designed to support the "alternating band" layout strategy. 

- **Primary & Secondary:** Ink (`#000000`) and Mint (`#c8f6f9`) are the workhorses. Mint is used specifically for high-energy secondary CTAs and stat highlights, particularly when set against dark backgrounds to maximize vibration and visibility.
- **Surface Polarity:** Use `deep-canvas` (`#010120`) for high-performance sections like workout tracking or research deep-dives. Use `canvas` (`#FFFFFF`) for administrative, settings, or lead-text areas.
- **The Gradient:** The Orange-Magenta-Periwinkle gradient is reserved for "Success States" (e.g., Personal Bests, Goal completion) and subtle brand accents. It should never be used for primary text.
- **Borders:** Use `hairline-light` on white surfaces and `hairline-dark` on dark surfaces to maintain structural integrity without adding visual weight.

## Typography
This design system utilizes a rigid functional split between Display and Monospace families.

- **Headline/Display (Sora):** Used for all narrative, titles, and emotional messaging. It represents the "Human" side of the app. For mobile, avoid sizes above 40px except for specialized stat dashboards.
- **Body (Inter):** Used for standard paragraphs and data descriptions to ensure maximum legibility during movement.
- **Label/Mono (JetBrains Mono):** This is the "Technical" voice. It is strictly reserved for buttons, eyebrows, and technical data points. **All labels and button text must be uppercase.**
- **Scale:** On mobile devices, use `display-md` for standard card titles and `eyebrow-mono` for section headers.

## Layout & Spacing
The layout follows a **Fluid Grid** model optimized for the mobile Android environment (Material 3 compatible). 

- **Grid:** Use a standard 4-column grid for mobile with 16px margins and 16px gutters.
- **Rhythm:** Vertical spacing should follow the 4px base unit. Use `3xl` (32px) for internal padding within dashboard cards to provide a premium, spacious feel.
- **Bands:** The "alternating band" strategy translates to mobile as full-width sections. Transitions between a light band and a dark band should have `section-gap` (48px) of padding to clearly signal context shifts.
- **Touch Targets:** Ensure all interactive mono-labels are wrapped in a container providing at least 48x48px of touch area, even if the visual label is smaller.

## Elevation & Depth
This design system is intentionally **Flat**. We reject the use of traditional shadows in favor of **Tonal Layers** and **Polarity**.

- **Surface Tiers:** Depth is created by placing `surface-soft` or `mint-accent` elements on top of `deep-canvas`.
- **Borders as Dividers:** Use 1px hairlines to define card boundaries. Shadows are only permitted for floating action buttons or transient "Toast" notifications, where they should be soft and tinted with the `deep-canvas` color at 15% opacity.
- **Active States:** Instead of "lifting" an element, indicate interaction via color inversion (e.g., a white button turning black upon press).

## Shapes
The shape language is **Soft (0.25rem)**, reflecting a precision instrument.

- **Standard Elements:** Buttons, cards, and input fields use a consistent 4px (`sm`) radius. This provides a subtle "human" touch while maintaining a disciplined, technical silhouette.
- **Navigation/Pills:** Use 8px (`md`) for toggle pills to distinguish them from primary action buttons.
- **Full Rounding:** Only permitted for circular icon buttons (e.g., a "Start Run" FAB) or progress trackers.
- **Bands:** Major layout containers (Section bands) must always have 0px roundedness to span the full width of the screen seamlessly.

## Components

### Buttons
- **Primary:** Solid `primary` (Ink) background with `on-primary` (Canvas) text. `button-mono` typography.
- **Secondary (Dark Surface):** Solid `mint-accent` background with `primary` (Ink) text. Used for high-priority actions within dark bands.
- **Ghost/Outline:** 1px hairline border in `neutral` (Body) with `primary` text.

### Cards
- **Mobile Card:** 16px (`lg`) internal padding. 1px hairline border. Background corresponds to the band it sits within (e.g., a `surface-soft` card on a `deep-canvas` band).
- **Stat Tiles:** Large `display-xl` numbers paired with `eyebrow-mono` labels.

### Input Fields
- **Technical Inputs:** 1px hairline border using `hairline-light`. Label sits above the field using `eyebrow-mono`. Text inside uses `body-md`. 4px border radius.

### Chips & Badges
- **Status Badges:** Small, 2px vertical / 8px horizontal padding. Use `hairline-light` background for inactive states and `mint-accent` for active/positive states.

### Banners
- **Performance Banner:** Full-width, `deep-canvas` background. Use `display-md` for messaging and a `mint-accent` secondary button.
- **Wordmark Banner:** A distinctive footer element using `display-xxl` text in `neutral` color, aligned to the bottom to ground the experience.