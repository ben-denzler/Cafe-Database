-- Used for displaying the 5 most recent orders
CREATE INDEX timeStampIndex ON Orders (timeStampRecieved);

-- Used for looking up a user's orders
CREATE INDEX loginIndex1 ON Orders (login);

-- Used for logging in users
CREATE INDEX loginIndex2 ON Users (login);

-- Used for modifying/displaying orders
CREATE INDEX orderIDIndex ON ItemStatus (orderid);