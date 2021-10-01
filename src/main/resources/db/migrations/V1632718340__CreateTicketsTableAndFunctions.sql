CREATE SCHEMA IF NOT EXISTS tickets;

CREATE SEQUENCE IF NOT EXISTS tickets.order_id
 INCREMENT BY 1
 MINVALUE 1
 NO MAXVALUE
 CACHE 1
 NO CYCLE
 OWNED BY NONE;


CREATE TABLE IF NOT EXISTS tickets.orders (
  id BIGINT DEFAULT nextval('tickets.order_id'::regclass) PRIMARY KEY NOT NULL,
  title varchar(64) NOT NULL,
  reservation_date date NOT NULL DEFAULT now()::date,
  performance_date date NOT NULL
);

DROP FUNCTION IF EXISTS tickets.get_reserved_tickets(varchar(32), date, date);

DROP TYPE IF EXISTS tickets.reservation_details;

CREATE TYPE tickets.reservation_details AS (
	tickets_reserved_count_query_date bigint,
	tickets_reserved_count_total bigint
);

CREATE OR REPLACE FUNCTION tickets.get_reserved_tickets(p_title varchar(32), p_query_date date, p_performance_date date)
RETURNS tickets.reservation_details
 LANGUAGE plpgsql
AS $$
DECLARE
  reserved_count tickets.reservation_details;

BEGIN
	SELECT count(*) INTO reserved_count.tickets_reserved_count_query_date
	FROM tickets.orders
	WHERE title = p_title AND reservation_date = p_query_date AND performance_date = p_performance_date;

	SELECT count(*) INTO reserved_count.tickets_reserved_count_total
	FROM tickets.orders
	WHERE title = p_title AND performance_date = p_performance_date;

	RETURN reserved_count;
END;
$$;


DROP FUNCTION IF EXISTS tickets.get_reserved_tickets_bulk(date);

DROP FUNCTION IF EXISTS tickets.get_reserved_tickets_bulk_on_day(date, date);

DROP TYPE IF EXISTS tickets.tickets_reserved;

CREATE TYPE tickets.tickets_reserved AS (
	show_title varchar(64),
	tickets_reserved_count bigint
);


-- function for creating get reserved tickets count on a specific performance day for each show
CREATE OR REPLACE FUNCTION tickets.get_reserved_tickets_bulk(p_performance_date date)
RETURNS SETOF tickets.tickets_reserved
 LANGUAGE plpgsql
AS $$
BEGIN
	RETURN QUERY
	SELECT o.title, count(*) as count
    FROM tickets.orders AS o
    WHERE o.performance_date = p_performance_date
    GROUP BY o.title;
END;
$$;

-- function for creating get reserved tickets count reserved on a specific date for a specific performance day for each show
CREATE OR REPLACE FUNCTION tickets.get_reserved_tickets_bulk_on_day(p_query_date date, p_performance_date date)
RETURNS SETOF tickets.tickets_reserved
 LANGUAGE plpgsql
AS $$
BEGIN
	RETURN QUERY
	SELECT o.title, count(*) as count
    FROM tickets.orders AS o
    WHERE o.performance_date = p_performance_date AND o.reservation_date = p_query_date
    GROUP BY o.title;
END;
$$;
