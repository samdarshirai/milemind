---
name: Kinetic Precision
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
  secondary: '#5a5b7f'
  on-secondary: '#ffffff'
  secondary-container: '#d4d4fe'
  on-secondary-container: '#595a7e'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#15124d'
  on-tertiary-container: '#7f7dbd'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2e2e2'
  primary-fixed-dim: '#c6c6c6'
  on-primary-fixed: '#1b1b1b'
  on-primary-fixed-variant: '#474747'
  secondary-fixed: '#e1e0ff'
  secondary-fixed-dim: '#c3c3ec'
  on-secondary-fixed: '#171838'
  on-secondary-fixed-variant: '#424466'
  tertiary-fixed: '#e2dfff'
  tertiary-fixed-dim: '#c3c0ff'
  on-tertiary-fixed: '#15124d'
  on-tertiary-fixed-variant: '#41407b'
  background: '#fbf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e2'
  canvas-dark: '#010120'
  ink: '#000000'
  hairline: rgba(0, 0, 0, 0.08)
  hairline-on-dark: rgba(255, 255, 255, 0.12)
  accent-orange: '#fc4c02'
  accent-magenta: '#ef2cc1'
  accent-periwinkle: '#bdbbff'
  mint-surface: '#c8f6f9'
typography:
  display-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '500'
    lineHeight: 48px
    letterSpacing: -0.8px
  display-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 32px
    letterSpacing: -0.42px
  display-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 25px
    letterSpacing: -0.22px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 23px
    letterSpacing: -0.18px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 21px
    letterSpacing: -0.16px
  mono-button:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.08px
  mono-eyebrow:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 11px
    letterSpacing: 0.55px
  mono-metric:
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
  xl: 24px
  2xl: 32px
  section: 48px
---

## Brand & Style

The design system for MileMind is rooted in a **Technical/High-Contrast** aesthetic that bridges the gap between elite athletic performance and high-scale infrastructure. It evokes a sense of "research-led" training, where every mile is treated as a data point.

The visual language is defined by extreme polarity—alternating between deep, immersive navy-blacks for headers and "focus" sections, and clinical, bright whites for data-heavy content. The style is primarily **Minimalist-Technical**, utilizing sharp hairlines, monospaced typography for utility, and a singular, vibrant "chrome" gradient to indicate momentum and progress.

## Colors

This design system uses a high-contrast polarity strategy. 
- **Dark Mode Surfaces:** Use `canvas-dark` (#010120) for Hero sections, Run Tracking headers, and immersive state-of-the-art summary bands.
- **Light Mode Surfaces:** Use pure white (#ffffff) for the primary feed, workout logs, and settings.
- **Interactive Elements:** All primary CTAs use `ink` (#000000) on light surfaces.
- **The Gradient:** A fixed three-stop gradient (`accent-orange` → `accent-magenta` → `accent-periwinkle`) is the only decorative element. Use it exclusively for progress bars, active run paths on maps, and subtle accent ribbons in the hero area.

## Typography

The system relies on a high-contrast pairing of Inter (Display Sans) for narrative/stats and JetBrains Mono (Monospace) for all technical labels and interactions.

**Key Rules:**
- **Metrics & Headlines:** Use Inter with tight letter-spacing for large-scale run stats (Distance, Pace).
- **Interactive Labels:** All buttons, chips, and navigation labels must use **uppercase Monospace**.
- **Metadata:** Use `mono-eyebrow` for section headers like "SPLITS," "HEART RATE ZONE," or "UPCOMING WORKOUTS."

## Layout & Spacing

The layout follows a rigorous **4px grid system**. 

- **Page Margins:** 24px (`xl`) on mobile; 32px (`2xl`) on tablet.
- **Sectioning:** Content is organized into "bands." Transition between dark and light bands to separate the "Live Run" view from "Post-Run Analysis."
- **Internal Padding:** Cards and containers should consistently use 24px (`xl`) interior padding to maintain a premium, airy feel despite the dense data.

## Elevation & Depth

This design system is **flat and structural**. Depth is achieved through color blocks rather than shadows.

- **Surface Tiers:** Use `canvas-dark` for primary elevation (Level 0) in headers. On light surfaces, use `hairline` (8% Black) borders to define containers.
- **The "Together" Shadow:** A single, very soft shadow (`rgba(1, 1, 32, 0.1) 0px 4px 10px`) is reserved exclusively for floating action buttons or temporary toast notifications.
- **Dividers:** Use 1px hairlines for all list items and table rows. Never use heavy drop shadows for cards.

## Shapes

The shape language is precise and disciplined. 

- **Primary Radius:** A 4px (`sm`) radius is the "canonical" corner for almost all elements including cards, input fields, and standard buttons.
- **Pills:** Large 9999px pill shapes are reserved exclusively for the "Black Ink" Primary CTA buttons (e.g., "START RUN").
- **Secondary Shapes:** Small 4px radius boxes are used for badges (e.g., "PR," "Laps").

## Components

- **Buttons:** Primary CTAs are full-pill "Ink Black" shapes with white uppercase monospace text. Secondary buttons use a 4px radius with a hairline border.
- **Cards:** Must have a 4px radius and a 1px hairline border (`rgba(0,0,0,0.08)` on light, `rgba(255,255,255,0.12)` on dark). No shadows. 
- **Progress Bars:** Utilize the orange-magenta-periwinkle gradient for the "filled" state. The track should be a faint version of the background or a subtle grey.
- **Chips/Badges:** Small rectangular shapes with 4px radius. Use `accent-periwinkle` or `accent-mint` for tinted backgrounds on status badges.
- **Inputs:** Clean, 1px bordered boxes with 4px radius. Labels should always be in `mono-eyebrow` style above the field.
- **Stat Tiles:** Large Inter-based numbers paired with small Monospace labels. Use `accent-periwinkle` tint for high-priority metrics like "Total Miles."