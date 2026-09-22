package com.example.model

enum class CoreComponent(
    val displayName: String,
    val technicalName: String,
    val role: String,
    val energyConversionRole: String,
    val primaryEquation: String,
    val normalOperatingRange: String,
    val cutawayDescription: String,
    val safetyThresholds: String
) {
    DAM(
        displayName = "Dam & Reservoir",
        technicalName = "Concrete Arch-Gravity Barrier & Headpond",
        role = "Stores water and impounds hydraulic potential energy head.",
        energyConversionRole = "Hydraulic Potential Energy (Ep = m·g·h)",
        primaryEquation = "Ep = ρ · V · g · H_gross",
        normalOperatingRange = "Water Level: 175 - 215 m (Max: 220 m)",
        cutawayDescription = "Heavy reinforced concrete structure featuring high-pressure trash racks, intake gates, and emergency spillway crest radial gates.",
        safetyThresholds = "Min Operating Head: 130m | Surcharge Level: 218m"
    ),
    PENSTOCK(
        displayName = "Penstock",
        technicalName = "High-Pressure Steel-Lined Conduit",
        role = "Chunnels reservoir water down steep elevation gradient, converting static head into dynamic kinetic and pressure energy.",
        energyConversionRole = "Hydrodynamic Kinetic & Pressure Energy",
        primaryEquation = "H_net = H_gross - f · (L/D) · (v² / 2g)",
        normalOperatingRange = "Velocity: 3.5 - 6.8 m/s | Pressure: 16 - 22 Bar",
        cutawayDescription = "High-tensile welded ASTM A516 steel conduit embedded in bedrock with acoustic flow meters and expansion couplings.",
        safetyThresholds = "Water Hammer Surge Limit: 26.5 Bar | Vacuum Collapse Risk < 0.5 Bar"
    ),
    MAIN_INLET_VALVE(
        displayName = "Main Inlet Valve (MIV)",
        technicalName = "Bi-Directional Spherical Isolating Valve",
        role = "Provides safe hydraulic isolation of the spiral casing and turbine pit for shutdown, maintenance, and emergency overspeed trip.",
        energyConversionRole = "Flow Regulation & Kinetic Channelling",
        primaryEquation = "Q = C_v · A_valve · √(2g · ΔH)",
        normalOperatingRange = "Opening: 0% (Closed) to 100% (Open) | Seal Pressure: 25 Bar",
        cutawayDescription = "Forged steel spherical plug actuated by double-acting hydraulic servomotors with weighted mechanical failsafe gravity counterweight and bypass valve.",
        safetyThresholds = "Emergency Closure Time: 8.5s | Max Housing Leakage: 2.0 L/min"
    ),
    TURBINE(
        displayName = "Turbine",
        technicalName = "Francis Reaction Runner & Spiral Casing",
        role = "Extracts energy from pressurized water swirl via wicket gates, transferring rotational torque to the runner blades.",
        energyConversionRole = "Mechanical Rotational Energy (Pm = τ · ω)",
        primaryEquation = "P_mech = ρ · g · Q · H_net · η_turbine",
        normalOperatingRange = "Speed: 375 RPM (50 Hz) | Efficiency: 91 - 94.5%",
        cutawayDescription = "13Cr4Ni martensitic stainless steel runner with 15 curved blades inside a volute spiral casing with 24 regulated distributor wicket gates.",
        safetyThresholds = "Cavitation Index Thoma σ > 0.08 | Overspeed Trip: 450 RPM (120%)"
    ),
    SHAFT(
        displayName = "Shaft Assembly",
        technicalName = "Vertical Forged Steel Torque Coupling",
        role = "Directly transmits mechanical rotational torque from the hydraulic turbine runner to the generator rotor.",
        energyConversionRole = "Mechanical Torque Transmission",
        primaryEquation = "τ = P_mech / (2π · N / 60)",
        normalOperatingRange = "Vibration: 0.8 - 2.2 mm/s RMS | Bearing Temp: 45 - 62°C",
        cutawayDescription = "Single-piece forged carbon-alloy steel shaft (650mm diameter) supported by babbitt-lined guide bearings with forced oil lubrication.",
        safetyThresholds = "Max Vibration Alarm: 4.5 mm/s | Trip: 7.0 mm/s | Bearing Temp Trip: 85°C"
    ),
    GENERATOR(
        displayName = "Generator",
        technicalName = "Vertical Salient-Pole Synchronous Alternator",
        role = "Converts mechanical shaft power into three-phase alternating electrical energy via electromagnetic induction.",
        energyConversionRole = "Electromagnetic Induction to 3-Phase AC",
        primaryEquation = "P_e = √3 · V_term · I_line · cos(φ)",
        normalOperatingRange = "Terminal Voltage: 13.8 kV | Active Power: 0 - 300 MW | 50.0 Hz",
        cutawayDescription = "16-pole rotor with laminated pole shoes, static thyristor excitation system, and mica-epoxy insulated stator windings with demineralized water cooling.",
        safetyThresholds = "Frequency Trip: <48.5 Hz or >51.5 Hz | Stator Winding Temp: 120°C"
    ),
    TRANSFORMER(
        displayName = "Transformer",
        technicalName = "Generator Step-Up (GSU) 3-Phase Unit",
        role = "Steps up generator terminal voltage from 13.8 kV to 230 kV high-voltage transmission level to minimize long-distance I²R transmission losses.",
        energyConversionRole = "Voltage Step-Up & Current Reduction (P = V · I)",
        primaryEquation = "V_s / V_p = N_s / N_p  (13.8 kV → 230 kV)",
        normalOperatingRange = "Secondary: 230 kV | Oil Temp: 50 - 68°C | Efficiency: 99.2%",
        cutawayDescription = "Oil-immersed forced-air cooled (ODAF) core-type transformer with on-load tap changer (OLTC) and Buchholz gas/oil surge relay.",
        safetyThresholds = "Top Oil Temp Trip: 95°C | Dissolved Gas Total: < 720 ppm"
    ),
    POWER_HOUSE(
        displayName = "Power House",
        technicalName = "Subterranean Machine Hall & Governor Centre",
        role = "Houses turbine pits, electro-hydraulic governors, SCADA control consoles, auxiliary backup diesels, and drainage sump systems.",
        energyConversionRole = "Facility Integration & Governor Closed-Loop Automation",
        primaryEquation = "f_error = f_actual - f_grid (PID Governor Loop)",
        normalOperatingRange = "Ambient Temp: 21 - 26°C | Sump Level: 15 - 35%",
        cutawayDescription = "Reinforced underground cavern (120m x 25m x 45m) with 250-ton overhead gantry crane, battery banks, and fire deluge suppression rings.",
        safetyThresholds = "Sump Flood Alarm > 70% | Governor Hydraulic Pressure > 120 Bar"
    ),
    SUBSTATION(
        displayName = "Substation",
        technicalName = "High-Voltage Switchyard & Dispatch Bay",
        role = "Connects generation to regional feeders, regulates power sales, and maintains grid stability via SF6 circuit breakers and busbars.",
        energyConversionRole = "Power Dispatch, Grid Synchronization & Revenue Generation",
        primaryEquation = "Revenue = Σ (MW_feeder_i · Tariff_i · Δt)",
        normalOperatingRange = "Busbar Voltage: 230 kV ± 5% | Grid Stability Index: 92 - 100%",
        cutawayDescription = "Gas-Insulated Switchgear (GIS) bay with disconnect switches, SF6 circuit breakers, lightning arresters, and synchrocheck relays.",
        safetyThresholds = "Short Circuit MVA Rating: 40 kA | Grid Instability Trip: Islanding Mode"
    )
}
