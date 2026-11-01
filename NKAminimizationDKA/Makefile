.PHONY: img clean

GRAPHS_DIR := ./app/graphs
GRAPH_NAMES := исходный_нка дка_перед_минимизацией минимизированный_дка

img:
	@set -e; command -v dot >/dev/null 2>&1 || { echo "Graphviz: команда dot не найдена" >&2; exit 1; }; \
	for file in "$(GRAPHS_DIR)"/*.dot; do \
		if [ -f "$$file" ]; then \
			dot -Tpng "$$file" -o "$${file%.dot}.png"; \
			test -s "$${file%.dot}.png"; \
			echo "Создано: $${file%.dot}.png"; \
		fi; \
	done

clean:
	@set -e; for name in $(GRAPH_NAMES); do \
		rm -f "$(GRAPHS_DIR)/$$name.png"; \
	done
