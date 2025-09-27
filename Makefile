run_homework:
	docker compose up --build

test_homework:
	docker compose --profile homework1_test run --rm --build homework1_test