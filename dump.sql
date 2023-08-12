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
    balance numeric DEFAULT 0 NOT NULL,
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
    START WITH 1003
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO banker;

--
-- Data for Name: account; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.account (id, balance) FROM stdin;
1002	0
1001	0.00
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: banker
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1.0.0	create account entity	SQL	V1_0_0__create_account_entity.sql	427695993	banker	2023-08-12 16:43:31.233953	11	t
2	1.0.1	create test users	SQL	V1_0_1__create_test_users.sql	275772206	banker	2023-08-12 16:43:31.256143	3	t
\.


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: banker
--

SELECT pg_catalog.setval('public.hibernate_sequence', 1003, false);


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
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: banker
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- PostgreSQL database dump complete
--

