-- ============================================================
-- Sandėliukas – papildomi duomenys
-- Įterpiama į: Product, Purchase, ShoppingCartItem,
--   Comment, Notification, NotificationRecipient,
--   LowStockItem, Forecast, Order, OrderProduct,
--   OrderInspection, ProfitabilityAnalysis,
--   Message, DayOff, WorkRecord
-- Vartotojų lentelė NEKEIČIAMA.
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- ============================================================
-- 1. RequiredQuantity  (reikalinga naujoms prekėms)
-- ============================================================
INSERT INTO `RequiredQuantity` (`id`, `quantity`) VALUES
(9,  80),
(10, 150),
(11, 40),
(12, 60);

-- ============================================================
-- 2. Product  (4 naujos prekės, id 9–12)
--    fk_Shelf naudoja esamas lentynas (1–8)
--    fk_Layout naudoja esamus ProductLayout (1–8)
-- ============================================================
INSERT INTO `Product`
  (`id`, `name`, `description`, `supplierPrice`, `initialStock`, `currentStock`,
   `isPublished`, `location`, `weight`, `volume`, `category`,
   `fk_Shelf`, `fk_RequiredQty`, `fk_Layout`)
VALUES
(9,  'Screwdriver Set 12pcs',
     '12-piece chrome-vanadium screwdriver set, flat and Phillips heads',
     11.9,  38, 38, 1, 'Zone 1 / Shelf 101', 0.45, 0.002, NULL, 1,  9, 1),
(10, 'Nylon Cable Ties 100pcs',
     'Pack of 100 black nylon cable ties, 300 mm length',
     3.8,  115, 115, 1, 'Zone 1 / Shelf 102', 0.25, 0.001, NULL, 2, 10, 3),
(11, 'Paint Roller Set 23cm',
     '23 cm paint roller with telescopic handle and plastic tray',
     9.5,   28,  28, 1, 'Zone 3 / Shelf 302', 0.70, 0.004, NULL, 6, 11, 5),
(12, 'Steel Measuring Tape 10m',
     'Self-locking steel measuring tape, 10 m, belt clip included',
     7.2,   62,  62, 1, 'Zone 3 / Shelf 301', 0.18, 0.001, NULL, 5, 12, 2);

-- ============================================================
-- 3. ProfitabilityAnalysis  (naujoms prekėms)
-- ============================================================
INSERT INTO `ProfitabilityAnalysis`
  (`id`, `storageCost`, `supplierTotal`, `sellingPrice`, `profit`, `isProfitable`, `fk_Product`)
VALUES
(9,  0.90, 11.9,  18.99, 6.19, 1, 9),
(10, 0.40,  3.8,   6.99, 2.79, 1, 10),
(11, 1.10,  9.5,  14.99, 4.39, 1, 11),
(12, 0.50,  7.2,  11.99, 4.29, 1, 12);

-- ============================================================
-- 4. Forecast  (naujoms prekėms, Q2 2024)
-- ============================================================
INSERT INTO `Forecast`
  (`id`, `periodStart`, `periodEnd`, `averageDemand`, `seasonalityCoeff`,
   `reorderPoint`, `safetyStock`, `createdAt`, `stockPosition`,
   `recommendedQuantity`, `fk_Product`)
VALUES
(9,  '2024-04-01', '2024-06-30',  8, 1,  60, 3, '2024-03-25', 38, 45, 9),
(10, '2024-04-01', '2024-06-30', 30, 1, 224, 12, '2024-03-25', 115, 37, 10),
(11, '2024-04-01', '2024-06-30',  5, 1,  37, 2, '2024-03-25', 28, 14, 11),
(12, '2024-04-01', '2024-06-30', 12, 1,  89, 5, '2024-03-25', 62, 29, 12);

-- ============================================================
-- 5. Order  (3 nauji užsakymai iš tiekėjo, id 7–9)
-- ============================================================
INSERT INTO `Order`
  (`id`, `arrivingQuantity`, `orderDate`, `arrivalDate`, `departureDate`,
   `deliveryDuration`, `received`, `sold`, `reservationDate`,
   `condition`, `paymentStatus`, `fk_Administrator`)
VALUES
(7, 60, '2024-04-01', '2024-04-08', '2024-04-01',  7, 60,  8, '2024-03-31', 1, 2, 'admin@warehouse.lt'),
(8, 200,'2024-04-15', '2024-04-25', '2024-04-15', 10, 190, 50, '2024-04-14', 1, 2, 'admin@warehouse.lt'),
(9, 45, '2024-05-01', NULL,          NULL,         14,  0,  0,  NULL,         1, 1, 'admin@warehouse.lt');

-- ============================================================
-- 6. OrderProduct  (prekės – užsakymai)
-- ============================================================
INSERT INTO `OrderProduct` (`fk_Order`, `fk_Product`) VALUES
(7,  9),   -- Screwdriver Set
(8,  10),  -- Cable Ties
(8,  11),  -- Paint Roller Set
(9,  12);  -- Measuring Tape

-- ============================================================
-- 7. OrderInspection  (patikros užsakymams 7 ir 8)
-- ============================================================
INSERT INTO `OrderInspection` (`id`, `date`, `note`, `condition`, `fk_Employee`, `fk_Order`)
VALUES
(5, '2024-04-08', 'All 60 screwdriver sets intact. No damage found.',              1, 'jonas.petraitis@warehouse.lt',    7),
(6, '2024-04-25', '190 units OK; 10 paint roller trays cracked in transit.',       2, 'ruta.kazlauskiene@warehouse.lt',  8);

-- ============================================================
-- 8. Purchase  (4 nauji pirkimai, id 10–13)
--    status: 1=Ordered, 2=At_Pickup_Point, 3=Picked_Up
--    paymentStatus: 1=Reserved, 2=Paid
-- ============================================================
INSERT INTO `Purchase`
  (`id`, `paymentDate`, `reservationDate`, `pickupDate`, `paymentStatus`, `status`, `fk_Buyer`)
VALUES
(10, '2026-04-28', '2026-04-27 14:30:00', '2026-04-30', 2, 3, 'laura.vaitkute@gmail.com'),
(11, '2026-05-02', '2026-05-01 10:15:00', NULL,          2, 2, 'paulius.bernotas@gmail.com'),
(12, '2026-05-05', '2026-05-04 16:00:00', '2026-05-07', 2, 3, 'agne.rimkute@gmail.com'),
(13, NULL,         '2026-05-16 09:00:00', NULL,          1, 1, 'tadpar@ktu.lt');

-- ============================================================
-- 9. ShoppingCartItem  (id 15–22)
--    bought=1 → rezervuota / apmokėta
--    bought=0 → krepšelyje, bet dar nerezervuota
-- ============================================================
INSERT INTO `ShoppingCartItem`
  (`id`, `quantity`, `bought`, `fk_Purchase`, `fk_Buyer`, `fk_Product`)
VALUES
-- Purchase 10: Laura nusiperka screwdrivers ir cable ties
(15, 2, 1, 10, 'laura.vaitkute@gmail.com',   9),
(16, 5, 1, 10, 'laura.vaitkute@gmail.com',  10),
-- Purchase 11: Paulius nusiperka gloves ir paint rollers
(17, 1, 1, 11, 'paulius.bernotas@gmail.com',  5),
(18, 2, 1, 11, 'paulius.bernotas@gmail.com', 11),
-- Purchase 12: Agnė nusiperka measuring tapes ir tiles
(19, 3, 1, 12, 'agne.rimkute@gmail.com',    12),
(20, 1, 1, 12, 'agne.rimkute@gmail.com',     7),
-- Purchase 13: Tadas rezervavo drill bits
(21, 2, 1, 13, 'tadpar@ktu.lt',              1),
-- Krepšelyje (nereservuota): dada@ktu.lt turi AA baterijų
(22, 3, 0, NULL, 'dada@ktu.lt',              4);

-- ============================================================
-- 10. Comment  (id 9–16, įvairūs produktai ir vartotojai)
-- ============================================================
INSERT INTO `Comment` (`id`, `text`, `creationDate`, `rating`, `fk_Product`, `fk_Buyer`)
VALUES
(9,  'Acetylene cylinder arrived well-packed and valve operates smoothly.',
     '2026-04-05', 4, 6,  'paulius.bernotas@gmail.com'),
(10, 'Screwdriver set is great value – all 12 pieces present and well-finished.',
     '2026-04-30', 5, 9,  'laura.vaitkute@gmail.com'),
(11, 'Cable ties grip firmly and dont snap under tension. Will reorder.',
     '2026-05-03', 4, 10, 'laura.vaitkute@gmail.com'),
(12, 'Paint roller applies evenly but the tray flexes too much. Average product.',
     '2026-05-06', 3, 11, 'paulius.bernotas@gmail.com'),
(13, 'Measuring tape is accurate and the lock holds perfectly at any length.',
     '2026-05-08', 5, 12, 'agne.rimkute@gmail.com'),
(14, 'Very satisfied with the drill bits – held up on both metal and hardwood.',
     '2026-04-16', 4, 1,  'paulius.bernotas@gmail.com'),
(15, 'Hydraulic jack is heavy to move around but performs flawlessly.',
     '2026-04-23', 4, 3,  'agne.rimkute@gmail.com'),
(16, 'Flood light is extremely bright. Installed in 10 minutes, no issues.',
     '2026-04-26', 5, 8,  'laura.vaitkute@gmail.com');

-- ============================================================
-- 11. Notification  (id 7–10)
-- ============================================================
INSERT INTO `Notification` (`id`, `title`, `body`) VALUES
(7,  'New Products Added',     '4 new products have been added to the catalog: Screwdriver Set, Cable Ties, Paint Roller Set, Measuring Tape.'),
(8,  'Low Stock Alert',        'Product "Paint Roller Set 23cm" stock is below minimum level.'),
(9,  'Purchase Completed',     'Purchase #10 by Laura Vaitkutė has been successfully paid and picked up.'),
(10, 'Incoming Shipment',      'Order #9 (Measuring Tape 10m x45) has been dispatched and is expected within 14 days.');

-- ============================================================
-- 12. NotificationRecipient
-- ============================================================
INSERT INTO `NotificationRecipient` (`fk_Notification`, `fk_User`) VALUES
(7, 'admin@warehouse.lt'),
(7, 'laura.vaitkute@gmail.com'),
(8, 'admin@warehouse.lt'),
(8, 'jonas.petraitis@warehouse.lt'),
(9, 'laura.vaitkute@gmail.com'),
(10, 'admin@warehouse.lt'),
(10, 'jonas.petraitis@warehouse.lt');

-- ============================================================
-- 13. LowStockItem  (id 5–6, naujos mažai sandėlyje)
-- ============================================================
INSERT INTO `LowStockItem` (`id`, `createdAt`, `fk_Product`) VALUES
(5, '2026-05-16', 6),   -- Acetylene Cylinder (stock=13)
(6, '2026-05-16', 11);  -- Paint Roller Set (stock=28)

-- ============================================================
-- 14. Message  (id 7–10)
-- ============================================================
INSERT INTO `Message`
  (`id`, `receiver`, `sender`, `body`, `sendDate`, `deleted`, `status`, `fk_Author`)
VALUES
(7,  'ruta.kazlauskiene@warehouse.lt', 'admin@warehouse.lt',
     'Rūta, please coordinate receipt of order #9 when it arrives next week.',
     '2024-05-01', 0, 2, 'admin@warehouse.lt'),
(8,  'admin@warehouse.lt', 'ruta.kazlauskiene@warehouse.lt',
     'Understood. I will prepare Zone 3 shelf space and notify you on arrival.',
     '2024-05-02', 0, 3, 'ruta.kazlauskiene@warehouse.lt'),
(9,  'tomas.jankauskas@warehouse.lt', 'admin@warehouse.lt',
     'Tomas, please inspect the paint roller shipment (order #8) upon arrival.',
     '2024-04-24', 0, 3, 'admin@warehouse.lt'),
(10, 'jonas.petraitis@warehouse.lt', 'admin@warehouse.lt',
     'Jonas, update stock counts in Zone 1 after screwdriver batch is shelved.',
     '2024-04-09', 0, 1, 'admin@warehouse.lt');

-- ============================================================
-- 15. DayOff  (id 6–8)
-- ============================================================
INSERT INTO `DayOff`
  (`id`, `startDate`, `endDate`, `notes`, `createdAt`, `updatedAt`, `type`, `fk_Owner`)
VALUES
(6, '2024-07-01', '2024-07-12', 'Kasmetinės atostogos', '2024-05-15', '2024-05-15', 1, 'ruta.kazlauskiene@warehouse.lt'),
(7, '2024-08-19', '2024-08-19', 'Asmeninė diena',       '2024-08-10', NULL,         3, 'jonas.petraitis@warehouse.lt'),
(8, '2025-01-01', '2025-01-01', 'Naujieji Metai',       '2024-01-10', NULL,         4, 'tomas.jankauskas@warehouse.lt');

-- ============================================================
-- 16. WorkRecord  (id 11–18, balandis 2024)
-- ============================================================
INSERT INTO `WorkRecord` (`id`, `date`, `duration`, `fk_Employee`) VALUES
(11, '2024-04-01', 480, 'jonas.petraitis@warehouse.lt'),
(12, '2024-04-02', 480, 'jonas.petraitis@warehouse.lt'),
(13, '2024-04-03', 360, 'ruta.kazlauskiene@warehouse.lt'),
(14, '2024-04-04', 480, 'tomas.jankauskas@warehouse.lt'),
(15, '2024-04-05', 480, 'jonas.petraitis@warehouse.lt'),
(16, '2024-04-05', 480, 'ruta.kazlauskiene@warehouse.lt'),
(17, '2024-04-07', 480, 'tomas.jankauskas@warehouse.lt'),
(18, '2024-04-08', 240, 'ruta.kazlauskiene@warehouse.lt');

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
