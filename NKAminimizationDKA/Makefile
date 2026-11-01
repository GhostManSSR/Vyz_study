.PHONY: img clean

GRAPHS_DIR := ./app/graphs

img:
	@for file in $(GRAPHS_DIR)/*.dot; do \
		if [ -f "$$file" ]; then \
			dot -Tpng "$$file" -o "$${file%.dot}.png"; \
			echo "Создано: $${file%.dot}.png"; \
		fi; \
	done

clean:
	rm -f $(GRAPHS_DIR)/*.png && rm -f $(GRAPHS_DIR)/*.dot