COPY MENU
-- FROM 'menu.csv'
FROM '/extra/bdenz001/needed_files/project/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
-- FROM 'users.csv'
FROM '/extra/bdenz001/needed_files/project/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
-- FROM 'orders.csv'
FROM '/extra/bdenz001/needed_files/project/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
-- FROM 'itemStatus.csv'
FROM '/extra/bdenz001/needed_files/project/data/itemStatus.csv'
WITH DELIMITER ';';