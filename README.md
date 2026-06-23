# ⚔️ Elden Ring Companion Tool

An interactive web-based companion for Elden Ring featuring a rune calculator, farm route guide, and quick access to community resources.

**Theme**: Shadow of the Erdtree Optimized  
**Styling**: Dark fantasy aesthetic with gold accents (Cinzel typography)

---

## 🎮 Features

### 📊 Rune Calculator
Calculate the total runes required to level from your current level to a target level.

- **Current Level**: Input your present character level (1-712)
- **Target Level**: Set your desired leveling goal (2-713)
- **Real-Time Calculation**: Instant rune requirement computation
- **Level Breakdown**: Shows total runes needed and average per level

**Formula Used**: Implements authentic Elden Ring leveling curve
- Levels 1-11: Hardcoded specific values
- Level 12+: `0.02L³ + 3.06L² + 105.6L - 895` (where L = level + 81)



### 🔗 Community Resources

#### Official Tools & Databases
- **🧭 Jerp Resources** - Essential game data and mechanics  
  https://jerp.tv/eldenring/

- **🗡️ Weapon Tools** - Attack Rating calculator and weapon comparisons  
  https://eldenring.tclark.io/

- **📊 Class Optimizer** - Min-maxing stats (Mugen Monkey)  
  https://mugenmonkey.com/eldenring

- **👗 Fashion Souls** - Armor aesthetics and fashion coordination  
  https://souls.fashion/eldenring

#### Trading & Community
- **🎭 Patches Emporium** - Reddit trade hub for item exchanges  
  https://www.reddit.com/r/PatchesEmporium/new/

#### Build Collections
- **📜 Plag Builds** - Expert-optimized loadouts and class templates  
  https://erhub.gamer.gd/pl.html

- **🛡️ More Builds** - Legacy archive of historical builds  
  https://erhub.gamer.gd/er2.html

- **🌳 Eden** - Curated resource and build hub  
  https://erhub.gamer.gd/eden.html

### ⚡ Quick Level Presets
One-click preset buttons for common meta levels:

- **LVL 125 (Meta)** - PvP and co-op standard
- **LVL 150 (Meta)** - High-tier PvP bracket
- **LVL 200 (NG+)** - New Game+ progression

---

## 🎨 Design

### Color Scheme
- **Gold**: `#d4af37` (Primary accent, Elden Ring aesthetic)
- **Background**: `#0c0c0e` (Deep black with subtle gradient)
- **Surface**: `#1a1a1c` (Card backgrounds)
- **Border**: `#3a3a3c` (Subtle dividers)
- **Text**: `#e5e5e5` (Light gray for readability)

### Typography
- **Headers**: Cinzel (serif, bold) — Fantasy/medieval aesthetic
- **Body**: Inter (sans-serif, 300-600 weight) — Clean, readable

### UI Elements
- **Cards**: Gold top border gradient, subtle shadow, rounded corners
- **Inputs**: Dark background with gold focus state and glow effect
- **Buttons**: Gold background with hover brightness and transform effects
- **Scrollbar**: Custom styled with gold on hover

---

## 📱 Responsive Design

- **Mobile**: Single column layout, optimized touch targets
- **Tablet**: Two-column staggered layout
- **Desktop**: Full two-column grid with optimal spacing

Grid breakpoint at `lg` (1024px)

---

## 🔧 How to Use

### Rune Calculator Tab (Default)
1. Enter your **Current Level** in the first field
2. Enter your **Target Level** in the second field
3. Click **Calculate Requirements**
4. View total runes needed and per-level average

### Quick Presets
- Click any preset button (125, 150, 200) to auto-fill and calculate instantly

---

## 💾 Technical Details

### JavaScript Functions


- Validates that target level > current level
- Sums rune costs for all intermediate levels
- Formats output with comma separators
- Displays result in dedicated panel

#### `applyPreset(cur, tar)`
Convenience function to set preset levels and trigger calculation.

---

## 🎯 Meta Levels Explained

### RL125 (PvP Meta)
Standard invasion and co-op level for most players. Balanced for fairness and matchmaking diversity.

### RL150 (High-Tier PvP)
Alternative meta for endgame-focused players. Supports more specialized builds with higher stat investment.

### RL200+ (NG+ Progression)
Personal leveling targets for New Game+ runs and solo challenge builds.

---

## 🚀 Installation & Deployment

### Local Use
1. Save the HTML file to your local machine
2. Open in any modern web browser (Chrome, Firefox, Safari, Edge)
3. No internet required except for external links

### Web Hosting
1. Upload to web server (static HTML, no backend required)
2. Serve with standard web server configuration
3. All styling and functionality self-contained

### Dependencies
- **Tailwind CSS** (via CDN): `https://cdn.tailwindcss.com`
- **Google Fonts** (Cinzel, Inter): CSS import via `fonts.googleapis.com`

---

## 📝 Customization Guide

### Change Color Scheme
Edit `:root` CSS variables at top of `<style>` block:

```css
:root {
    --er-gold: #d4af37;      /* Primary accent */
    --er-bg: #0c0c0e;        /* Page background */
    --er-surface: #1a1a1c;   /* Card background */
    --er-border: #3a3a3c;    /* Border color */
}
```

### Add New Resources
Add items to the resource grid in the HTML:

```html
<a href="your-url" target="_blank" class="p-3 border border-gray-800 hover:border-[#d4af37] transition-colors rounded flex items-center gap-3 group">
    <span class="text-2xl group-hover:scale-110 transition-transform">🎯</span>
    <div>
        <div class="text-sm font-bold text-gray-200">Your Resource</div>
        <div class="text-xs text-gray-500">Description</div>
    </div>
</a>
```

### Modify Farm Routes
Update the `farms` array in `populateFarm()` function:

```javascript
const farms = [
    { lvl: "Stage", loc: "Location Name", yield: "Runes & Duration" },
    // Add more...
];
```

### Update Rune Formula
If formula changes in game updates, modify `getLevelCost()` function with new coefficients.

---

## 🎓 Educational Notes

### Elden Ring Leveling Mechanics
- Each level costs progressively more runes
- Early levels are cheapest; cost scales cubically
- Soft caps at 80 for most stats; diminishing returns beyond

### Why These Meta Levels?
- **RL125**: Matches average playthrough completion (~60-80 hours)
- **RL150**: Allows specialized builds with cap-reaching stats
- **RL200+**: Personal choice; accepts longer matchmaking times

---

## 🔄 Updates & Maintenance

**Last Updated**: Shadow of the Erdtree (DLC Optimized)

### Future Enhancements
- [ ] Spell/Incantation requirement calculator
- [ ] Stat soft-cap visualizer
- [ ] Build comparison matrix
- [ ] Real-time PvP activity tracker
- [ ] Custom farming route planner

---

## 📄 License & Attribution

Developed for the Tarnished community. Based on authentic Elden Ring game mechanics.

**Disclaimer**: This is a fan-created tool. Not affiliated with FromSoftware or Bandai Namco.

**Footer Message**: *"Developed for the Tarnished • May Chaos Take the World"*

---

## 🎪 UI/UX Features

### Visual Feedback
- ✨ Hover effects on all interactive elements
- 🎯 Gold underline for active tabs
- 📍 Smooth transitions (0.2-0.3s) on all state changes
- 🌟 Glow effect on focused inputs

### Accessibility
- Semantic HTML structure
- ARIA-friendly button labels
- High contrast text (gold on black)
- Keyboard-navigable (standard HTML elements)

### Performance
- No external dependencies except CDN (Tailwind, Fonts)
- Zero build process required
- Sub-100KB file size
- Instant client-side calculations

---

---

## 🌐 External Resources & Links

### Essential Elden Ring Tools
| Tool | URL | Purpose |
|------|-----|---------|
| Jerp Data | https://jerp.tv/eldenring/ | Game mechanics, item data, scaling info |
| Weapon AR Calc | https://eldenring.tclark.io/ | Attack rating calculations, damage simulation |
| Mugen Monkey | https://mugenmonkey.com/eldenring | Class optimizer, stat allocation |
| Souls Fashion | https://souls.fashion/eldenring | Armor cosmetics gallery, fashion coordination |
| Patches Emporium | https://www.reddit.com/r/PatchesEmporium/new/ | Community item trading (Reddit) |

### Build & Reference Hubs
| Resource | URL | Specialization |
|----------|-----|-----------------|
| Plag Builds | https://erhub.gamer.gd/pl.html | 108+ class templates, RL125/150 optimized |
| Build Archives | https://erhub.gamer.gd/er2.html | PvE inventory layouts, stat breakdowns |
| Eden Hub | https://erhub.gamer.gd/eden.html | Curated builds, chronological collection |

### Community Platforms
| Platform | URL | Content |
|----------|-----|---------|
| r/Eldenring | https://www.reddit.com/r/Eldenring/ | General discussion, lore, clips |
| r/PatchesEmporium | https://www.reddit.com/r/PatchesEmporium/new/ | Item trading, co-op sessions |
| Discord Communities | Various | Real-time chat, PvP coordination |

---

## 🎯 Related Resources

### PvP & Competitive
- **Meta Level Standards**: 125, 150 (Matchmaking tiers)
- **Invasion Ranges**: https://jerp.tv/eldenring/ (view matchmaking FAQ)
- **PvP Discord Communities**: Check r/Eldenring sidebar

### Content Creators & Guides
- **YouTube Builds**: Search "Elden Ring RL125 build guide"
- **Speedrun Routes**: https://www.speedrun.com/eldenring
- **Challenge Runs**: RL1, SL1, No-Hit communities on Discord

---

**Contact & Support**:
- Discord: lenz5032 (build curator)
- Reddit: r/PatchesEmporium, r/Eldenring
- GitHub: Build collections and tool repositories

**Last Verified**: June 2026 (Shadow of the Erdtree patch)
