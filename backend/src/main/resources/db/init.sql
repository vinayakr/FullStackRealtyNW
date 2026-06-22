CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS articles (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    excerpt TEXT,
    content TEXT NOT NULL,
    author VARCHAR(100) DEFAULT 'Vinny Rao',
    category VARCHAR(100),
    image_url VARCHAR(500),
    read_time_minutes INTEGER DEFAULT 5,
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id SERIAL PRIMARY KEY,
    session_id UUID REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_articles_slug ON articles(slug);
CREATE INDEX IF NOT EXISTS idx_articles_category ON articles(category);

INSERT INTO articles (title, slug, excerpt, content, category, read_time_minutes) VALUES
(
  'Why 2% Commission Saves You Thousands in the Pacific Northwest',
  'why-2-percent-commission-saves-thousands',
  'Traditional real estate commissions eat 3% of your sale price on the listing side alone. Discover how Full Stack Realty NW puts thousands back in your pocket without sacrificing service.',
  '## The Commission Problem Nobody Talks About

When you sell your home, one of the largest costs you face is the real estate commission. Most sellers don''t realize the listing agent''s fee alone is typically 2.5–3% of the sale price — on top of the 2.5–3% buyer''s agent commission.

On a $650,000 home (close to the Pacific Northwest median), a 3% listing commission means **$19,500 out of your pocket** before you even negotiate repairs or closing costs.

## The Full Stack Realty NW Difference

We charge a **2% listing commission** — full stop. Here''s what that looks like:

| Home Price | Traditional (3%) | Full Stack Realty NW (2%) | You Save |
|-----------|-----------------|--------------------------|----------|
| $500,000  | $15,000         | $10,000                  | $5,000   |
| $650,000  | $19,500         | $13,000                  | $6,500   |
| $800,000  | $24,000         | $16,000                  | $8,000   |
| $1,000,000 | $30,000        | $20,000                  | $10,000  |

## Full Service, Not a Discount Experience

A lower commission never means lesser service. Here''s what you get:

**Professional Marketing Package**
- HDR photography and video walkthrough
- MLS listing with syndication to Zillow, Redfin, Realtor.com
- Targeted social media campaigns
- Professional yard signage and open house coordination

**Investor-Grade Pricing Strategy**
As both an active real estate investor and licensed agent, I bring a unique lens to pricing. I know what buyers — especially investors — are willing to pay, and I price your home to attract the right offers fast.

**Expert Negotiation**
Every offer gets analyzed for net proceeds, not just the headline price. I negotiate contingencies, timelines, and credits to maximize what you actually walk away with.

**End-to-End Transaction Management**
From listing to keys in hand, I coordinate inspections, title, escrow, and lender requirements — you just show up at closing.

## The Bottom Line

Saving 1% on your listing commission on a $700,000 home means **$7,000 more for your family**. That''s a year of college tuition, a new roof, or a significant down payment contribution on your next home.

Ready to see your numbers? [Start a conversation with our AI advisor](/chat) or email me directly at vinny@fullstackrealtynw.com.',
  'Selling',
  6
),
(
  'Top Neighborhoods for Families in the Pacific Northwest',
  'top-neighborhoods-families-pacific-northwest',
  'From Bellevue to Beaverton, the PNW offers incredible communities for families. Here are the neighborhoods our clients love most — and why.',
  '## Finding the Right Community for Your Family

The Pacific Northwest is one of the best places in the country to raise a family. Strong school districts, outdoor access, walkable downtowns, and a tech-driven economy make cities like Seattle, Bellevue, Kirkland, and Portland perennially popular. But which neighborhood is right for *your* family?

Here are the areas our clients consistently love — and what makes each one special.

## Greater Seattle Area

### Kirkland
Kirkland combines small-town charm with easy access to Microsoft''s Redmond campus and Seattle. The downtown waterfront is walkable and family-friendly, the school district is excellent, and the neighborhoods around Juanita Beach offer suburban comfort without feeling isolated.

**Ideal for:** Tech families, water lovers, those who want walkability without city density

### Bellevue (Somerset, Bridle Trails)
Bellevue''s east side neighborhoods offer some of the top-rated schools in Washington State. Somerset sits on a ridge with panoramic views, while Bridle Trails is horse-country tranquil despite being minutes from downtown Bellevue''s growing tech scene.

**Ideal for:** Families prioritizing school rankings, professionals in the Eastside tech corridor

### Issaquah / Sammamish Plateau
The Issaquah Highlands and Sammamish Plateau have exploded in the last decade — and for good reason. Newer construction, excellent schools, proximity to Tiger Mountain trail systems, and a tight-knit community feel.

**Ideal for:** Outdoor enthusiasts, families wanting newer homes, those with longer commute flexibility

### Redmond (Education Hill)
Sitting between Microsoft''s main campus and downtown Redmond, Education Hill offers tree-lined streets, excellent schools, and easy bike access to the Sammamish River Trail.

**Ideal for:** Microsoft employees, cyclists, families wanting a quieter pace near the city

## Eastside Suburbs (Snohomish County)

### Bothell / Mill Creek
These communities have grown into their own city centers while retaining suburban affordability relative to Bellevue. Bothell''s downtown has been revitalized, and Mill Creek Town Center gives the neighborhood a real gathering place.

**Ideal for:** Value-conscious buyers who don''t want to sacrifice school quality

## Portland Metro

### Lake Oswego
Lake Oswego consistently ranks among the top school districts in Oregon while maintaining a resort-town feel. Access to Oswego Lake is a lifestyle perk that draws families from across the metro.

**Ideal for:** Oregon-side families who want top schools and lake access

### West Linn
South of Lake Oswego, West Linn offers beautiful riverfront properties, excellent schools, and a more affordable entry point than its northern neighbor.

**Ideal for:** Families wanting river/nature access at slightly lower price points

## How to Find Your Right Neighborhood

The "best" neighborhood depends entirely on your family''s unique priorities. Our [AI home advisor](/chat) will walk you through exactly what matters to your family and match you with the communities that check your boxes.

As an investor who has owned properties across the PNW, I can give you unfiltered insight into each market — not just the marketing brochure version.',
  'Buying',
  7
),
(
  'Real Estate Investing 101: Building Wealth Through Property in the PNW',
  'real-estate-investing-101-pnw',
  'Real estate has created more millionaires than any other asset class. As an active investor in the Pacific Northwest market, here''s what I wish I knew when I started.',
  '## Why Real Estate Builds Wealth

Real estate is one of the few investments that combines multiple wealth-building mechanisms simultaneously:

1. **Appreciation** — Property values in the PNW have historically appreciated 4–7% annually over long time horizons
2. **Cash Flow** — Rental income that exceeds your costs creates passive monthly income
3. **Loan Paydown** — Your tenants are paying off your mortgage while you build equity
4. **Tax Advantages** — Depreciation, mortgage interest deductions, and 1031 exchanges are powerful tools
5. **Leverage** — You control a $500,000 asset with $100,000 down (5:1 leverage)

No other common investment class gives you all five at once.

## Starting Your Investment Journey

### Step 1: Define Your Strategy

There are three main residential investment strategies:

**Long-Term Rentals (Buy and Hold)**
Purchase a property, rent it to long-term tenants, and hold for appreciation while collecting monthly cash flow. This is the most passive strategy and ideal for beginners.

**House Hacking**
Purchase a 2–4 unit property, live in one unit, and rent the others. The rental income offsets your mortgage — sometimes entirely. This is how many investors get their first property at owner-occupant financing rates.

**BRRRR (Buy, Rehab, Rent, Refinance, Repeat)**
Buy distressed properties below market, renovate them, rent them, refinance to pull out capital, and repeat. This is how portfolios scale quickly, but requires more active involvement.

### Step 2: Understand Your Numbers

Every investment decision comes down to the numbers. Key metrics:

**Cap Rate** = Net Operating Income ÷ Property Value
A good cap rate in the PNW is typically 4–6% in urban areas, higher in suburban markets.

**Cash-on-Cash Return** = Annual Cash Flow ÷ Total Cash Invested
Target 6–10% cash-on-cash returns in today''s market.

**Gross Rent Multiplier (GRM)** = Purchase Price ÷ Annual Gross Rent
Lower GRM = better deal. The PNW typically sees GRMs of 15–20 in hot markets.

### Step 3: Choose Your Market

Not all PNW markets perform equally. Here''s a quick overview:

**Seattle Proper** — High appreciation, lower cap rates, strong rental demand. Regulatory environment favors tenants.

**Tacoma** — More affordable entry points, improving appreciation, strong rent growth as workers price out of Seattle.

**Renton / Kent / Auburn** — Value plays with strong working-class rental demand and improving infrastructure.

**Spokane** — Lower price points, higher cap rates, growing tech and healthcare economy. Often overlooked by Westside investors.

**Portland Metro** — Strong appreciation historically, but rent control laws require careful analysis.

## The Role of an Investor-Agent

Working with an agent who is also an active investor changes the conversation entirely. I can help you:

- Analyze deals with real investor math, not just comps
- Identify off-market opportunities before they hit the MLS
- Evaluate renovation costs before you make an offer
- Structure offers that win in competitive markets
- Build a property management strategy from day one

Ready to start investing? Let''s talk. Use our [AI advisor](/chat) to describe your investment goals, or email me at vinny@fullstackrealtynw.com.',
  'Investing',
  9
),
(
  'First-Time Homebuyer''s Complete Guide to the Pacific Northwest',
  'first-time-homebuyer-guide-pacific-northwest',
  'Buying your first home is one of the most exciting — and overwhelming — experiences of your life. This guide cuts through the noise and gives you a clear roadmap.',
  '## You''re Ready to Buy. Now What?

The Pacific Northwest housing market moves fast. In competitive areas, well-priced homes regularly receive multiple offers within days. As a first-time buyer, knowing the process before you start gives you a meaningful edge.

## Step 1: Get Pre-Approved (Not Just Pre-Qualified)

Pre-qualification is a quick estimate based on self-reported income. **Pre-approval** is a verified lender commitment — and in the PNW, sellers won''t take your offer seriously without it.

What you''ll need:
- Last 2 years of tax returns and W-2s
- 2 months of bank statements
- Recent pay stubs
- Authorization for a credit pull

Target a credit score above 720 for the best rates, though FHA loans are available with scores as low as 580.

## Step 2: Know Your True Budget

Your pre-approval amount is a ceiling, not a target. A lender will approve you for the maximum they''re comfortable with — but the right number is what you''re comfortable with.

Rule of thumb: your total housing costs (mortgage, taxes, insurance, HOA) should not exceed 28–30% of your gross monthly income.

Don''t forget closing costs: typically 2–3% of the purchase price in Washington and Oregon. On a $500,000 home, budget $10,000–$15,000 in addition to your down payment.

## Step 3: Define Your Must-Haves vs. Nice-to-Haves

Make two lists before you start touring homes:

**Non-negotiables** (deal-breakers if absent)
- Minimum bedrooms/bathrooms for your family
- School district requirements
- Maximum commute time
- Garage or parking requirements

**Nice-to-haves** (preferences, but flexible)
- Updated kitchen
- Large yard
- Home office space
- Proximity to parks

Having this clarity prevents emotional decisions in competitive bidding situations.

## Step 4: Understand the PNW Market

**Washington State:** No state income tax (major draw for California transplants), which keeps demand high. The Seattle-Eastside corridor is among the most expensive markets in the country. Tacoma, Olympia, and Eastern Washington offer more affordability.

**Oregon:** Portland''s rent control environment has softened some investor demand, creating slightly more buyer-friendly conditions. The Oregon coast and Southern Oregon are lifestyle markets with different dynamics.

**Both states:** Closing timelines are typically 30 days for standard transactions. Washington uses title/escrow companies; Oregon uses both title companies and attorneys.

## Step 5: Make Competitive Offers

In competitive markets, your offer strategy matters as much as your price. Key elements:

- **Escalation clauses** — automatically beat competing offers up to a ceiling
- **Pre-inspection** — inspect before offering to waive the inspection contingency confidently
- **Flexible closing** — match the seller''s preferred timeline
- **Personal letter** — still legal in Oregon; Washington prohibits some forms
- **Strong earnest money** — 2–3% shows commitment

## Step 6: Survive the Closing Process

Once under contract, you''ll have approximately 30 days to:
- Complete your formal loan application
- Complete inspections (even waiving repair requests, you want to know what you''re buying)
- Review title report for any liens or encumbrances
- Secure homeowner''s insurance
- Complete final walkthrough
- Sign closing documents

I guide every client through each of these milestones personally.

## Your Next Step

Use our [AI home advisor](/chat) to tell us about your family''s goals — we''ll help you narrow down neighborhoods, price ranges, and must-have features before we start touring. It''s the best way to make your home search efficient and successful.',
  'Buying',
  8
),
(
  'Understanding Pacific Northwest Real Estate Market Trends in 2024–2025',
  'pnw-real-estate-market-trends-2024-2025',
  'Interest rates, inventory shifts, and remote work patterns are reshaping the PNW market. Here''s what buyers and sellers need to know right now.',
  '## The Market Has Normalized — Here''s What That Means

After the frenzy of 2020–2022 and the correction of 2023, the Pacific Northwest real estate market has settled into a more balanced state. That''s good news for everyone — but "balanced" doesn''t mean static, and understanding current trends is essential whether you''re buying, selling, or investing.

## Interest Rate Reality

The Federal Reserve''s rate cycle has pushed 30-year fixed mortgage rates into the 6.5–7.5% range — a significant adjustment from the sub-3% environment of 2021. Here''s how to think about it:

**For buyers:** Higher rates reduce purchasing power, but they also cooled competition. Multiple-offer frenzies are less common today. Many buyers are also using adjustable-rate mortgages (ARMs) and planning to refinance when rates drop.

**For sellers:** The "lock-in effect" is real — many homeowners with 2.5–3% mortgages are reluctant to sell and give up that rate. This has constrained inventory, which has partially offset the reduced demand from higher rates.

**The refinance opportunity:** Many analysts expect rates to moderate over the next 2–3 years. Buyers who purchase today at 7% may be able to refinance to 5.5–6% within a few years, meaningfully improving their monthly payment.

## Inventory Trends by Market

**Seattle/Eastside:** Inventory remains tight relative to demand. Tech employment at Amazon, Microsoft, and the growing startup ecosystem continues to attract high-income buyers. Condos have more inventory than single-family homes.

**Tacoma:** One of the stronger appreciation markets in the state as buyers seek affordability relative to Seattle. Inventory has improved but quality listings still move quickly.

**Portland Metro:** More buyer-friendly than most PNW markets due to some outmigration and the high-rate environment. Investors have pulled back due to rent control regulations, creating more opportunity for owner-occupants.

**Spokane:** Inventory has improved significantly. The market is more negotiable than 2021–2022, and Spokane''s growing healthcare and education sectors support long-term demand.

**Eastern Oregon:** Markets like Bend remain highly desirable lifestyle destinations with constrained inventory. Prices are elevated but stabilizing.

## The Remote Work Factor

Remote and hybrid work remains a structural feature of the PNW economy — not a temporary trend. This continues to support:

- Demand for home offices (often non-negotiable for buyers now)
- Interest in smaller cities within 2–3 hours of major metros
- Larger homes and yard space over urban density

## What This Means for You

**If you''re selling:** Price correctly from day one. Overpriced listings sit longer and often sell for less than a well-priced home that generates competition. My investor background means I help you price with real market data, not wishful thinking.

**If you''re buying:** This is actually a good window. Less competition, more negotiating room, and sellers who are genuinely motivated. If rates drop and inventory doesn''t improve, today''s prices look attractive in retrospect.

**If you''re investing:** Evaluate deals on current-rate economics, not 2021 assumptions. Markets with strong rent growth (Tacoma, Spokane, secondary Oregon markets) offer better current yields than the Seattle core.

Let''s talk about your specific situation. Use our [AI advisor](/chat) or reach out directly at vinny@fullstackrealtynw.com.',
  'Market Insights',
  7
);
