--
-- PostgreSQL database dump
--

-- Dumped from database version 15.1
-- Dumped by pg_dump version 15.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account; Type: TABLE; Schema: public; Owner: banker
--

CREATE TABLE public.account (
    id bigint NOT NULL,
    balance numeric(15,2) DEFAULT 0 NOT NULL,
    CONSTRAINT account_balance_check CHECK ((balance >= (0)::numeric))
);


ALTER TABLE public.account OWNER TO banker;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: banker
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO banker;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: banker
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO banker;

--
-- Name: operation; Type: TABLE; Schema: public; Owner: banker
--

CREATE TABLE public.operation (
    id bigint NOT NULL,
    account_id bigint NOT NULL,
    type character varying(1) NOT NULL,
    amount numeric(15,2) NOT NULL,
    date timestamp without time zone NOT NULL,
    CONSTRAINT operation_amount_check CHECK ((amount > (0)::numeric))
);


ALTER TABLE public.operation OWNER TO banker;

--
-- Name: transfer; Type: TABLE; Schema: public; Owner: banker
--

CREATE TABLE public.transfer (
    id bigint NOT NULL,
    outgoing_transfer_id bigint NOT NULL,
    incoming_transfer_id bigint NOT NULL
);


ALTER TABLE public.transfer OWNER TO banker;

--
-- Data for Name: account; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.account (id, balance) FROM stdin;
1001	0.00
1002	0.00
1003	10.00
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1.0.0	create account entity	SQL	V1_0_0__create_account_entity.sql	-234539271	banker	2023-09-03 20:18:50.952457	28	t
2	1.0.1	create test users	SQL	V1_0_1__create_test_users.sql	275772206	banker	2023-09-03 20:18:51.003947	2	t
3	1.1.0	create operation entity	SQL	V1_1_0__create_operation_entity.sql	130108117	banker	2023-09-03 20:18:51.012839	4	t
4	1.1.1	create test operations history	SQL	V1_1_1__create_test_operations_history.sql	1443040724	banker	2023-09-03 20:18:51.033519	4	t
5	1.2.0	create transfer entity	SQL	V1_2_0__create_transfer_entity.sql	941021906	banker	2023-09-03 20:18:51.043534	7	t
6	1.2.1	change numeric scale	SQL	V1_2_1__change_numeric_scale.sql	2138348207	banker	2023-09-03 20:18:51.057786	12	t
\.


--
-- Data for Name: operation; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.operation (id, account_id, type, amount, date) FROM stdin;
1004	1003	D	10.00	2023-01-01 00:00:00
1005	1003	D	10.00	2023-02-01 00:00:00
1006	1003	W	1.00	2023-03-01 00:00:00
1007	1003	W	1.00	2023-04-01 00:00:00
1008	1003	W	1.00	2023-05-01 00:00:00
1009	1003	W	1.00	2023-06-01 00:00:00
1010	1003	W	1.00	2023-07-01 00:00:00
1011	1003	W	1.00	2023-08-01 00:00:00
1012	1003	W	1.00	2023-09-01 00:00:00
1013	1003	W	1.00	2023-10-01 00:00:00
1014	1003	W	1.00	2023-11-01 00:00:00
1015	1003	W	1.00	2023-12-01 00:00:00
\.


--
-- Data for Name: transfer; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.transfer (id, outgoing_transfer_id, incoming_transfer_id) FROM stdin;
\.


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: banker
--

SELECT pg_catalog.setval('public.hibernate_sequence', 1100, false);


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: operation operation_pkey; Type: CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.operation
    ADD CONSTRAINT operation_pkey PRIMARY KEY (id);


--
-- Name: transfer transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.transfer
    ADD CONSTRAINT transfer_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: banker
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: operation_account_id_idx; Type: INDEX; Schema: public; Owner: banker
--

CREATE INDEX operation_account_id_idx ON public.operation USING btree (account_id);


--
-- Name: transfer_incoming_transfer_id_idx; Type: INDEX; Schema: public; Owner: banker
--

CREATE INDEX transfer_incoming_transfer_id_idx ON public.transfer USING btree (incoming_transfer_id);


--
-- Name: transfer_outgoing_transfer_id_idx; Type: INDEX; Schema: public; Owner: banker
--

CREATE INDEX transfer_outgoing_transfer_id_idx ON public.transfer USING btree (outgoing_transfer_id);


--
-- Name: operation operation_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.operation
    ADD CONSTRAINT operation_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(id);


--
-- Name: transfer transfer_incoming_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.transfer
    ADD CONSTRAINT transfer_incoming_transfer_id_fkey FOREIGN KEY (incoming_transfer_id) REFERENCES public.operation(id);


--
-- Name: transfer transfer_outgoing_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: banker
--

ALTER TABLE ONLY public.transfer
    ADD CONSTRAINT transfer_outgoing_transfer_id_fkey FOREIGN KEY (outgoing_transfer_id) REFERENCES public.operation(id);


--
-- PostgreSQL database dump complete
--

