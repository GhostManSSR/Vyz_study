import React, {useState} from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';
import Main from "./pages/main";
import assert, {equal} from "node:assert";

test('renders learn react link', () => {
  render(<App />);
  const linkElement = screen.getByText(/Test API/i);
  expect(linkElement).toBeInTheDocument();
});


test('test config', () => {
  render(<App />);
  const linkElement = screen.getByText(/Test API/i);
  equal(15,15,"true")
})